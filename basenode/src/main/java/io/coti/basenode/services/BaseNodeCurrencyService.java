package io.coti.basenode.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.CurrencyTypeRegistrationCrypto;
import io.coti.basenode.crypto.GetUserTokensRequestCrypto;
import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.CurrencyException;
import io.coti.basenode.http.*;
import io.coti.basenode.http.data.TokenResponseData;
import io.coti.basenode.http.data.TransactionResponseData;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.model.CurrencyNameIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.model.UserCurrencyIndexes;
import io.coti.basenode.services.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;

@Slf4j
@Service
public class BaseNodeCurrencyService implements ICurrencyService {

    public static final String ERROR_AT_GETTING_USER_TOKENS = "Error at getting user tokens: ";
    @Value("${native.currency.name}")
    protected String nativeCurrencyName;
    @Value("${native.currency.symbol}")
    protected String nativeCurrencySymbol;
    private Hash nativeCurrencyHash;
    @Autowired
    protected Currencies currencies;
    @Autowired
    protected CurrencyNameIndexes currencyNameIndexes;
    @Autowired
    protected INetworkService networkService;
    @Autowired
    protected RestTemplate restTemplate;
    @Autowired
    protected ApplicationContext applicationContext;
    @Autowired
    protected CurrencyTypeRegistrationCrypto currencyTypeRegistrationCrypto;
    @Autowired
    private GetUserTokensRequestCrypto getUserTokensRequestCrypto;
    @Autowired
    protected IBalanceService balanceService;
    @Autowired
    private UserCurrencyIndexes userCurrencyIndexes;
    @Autowired
    protected IEventService eventService;
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private Transactions transactions;
    private final LockData currencyLockData = new LockData();
    private final LockData currencyNameLockData = new LockData();
    private final LockData originatorHashLockData = new LockData();
    private final LockData mintingTokenHashLockData = new LockData();
    private Map<Hash, BigDecimal> currencyHashToMintableAmountMap;
    private Map<Hash, Set<TransactionData>> postponedTokenMintingTransactionsMap;
    private Map<Hash, Boolean> mintingTransactionToConfirmationMap;
    private Map<Hash, Set<Hash>> tokenTransactionHashesMap;

    public void init() {
        currencyHashToMintableAmountMap = new ConcurrentHashMap<>();
        postponedTokenMintingTransactionsMap = new ConcurrentHashMap<>();
        mintingTransactionToConfirmationMap = new ConcurrentHashMap<>();
        tokenTransactionHashesMap = new ConcurrentHashMap<>();
        try {
            setNativeCurrencyHashFromSymbol();
            log.info("{} is up", this.getClass().getSimpleName());
        } catch (CurrencyException e) {
            throw new CurrencyException("Error at currency service init.\n" + e.getMessage(), e);
        } catch (Exception e) {
            throw new CurrencyException("Error at currency service init.", e);
        }
    }

    private void setNativeCurrencyHashFromSymbol() {
        if (StringUtils.isEmpty(nativeCurrencySymbol)) {
            throw new CurrencyException("Native currency Symbol is missing.");
        }
        nativeCurrencyHash = OriginatorCurrencyCrypto.calculateHash(nativeCurrencySymbol);
    }

    @Override
    public BigDecimal getTokenMintableAmount(Hash tokenHash) {
        if (currencyHashToMintableAmountMap.get(tokenHash) != null) {
            return new BigDecimal(currencyHashToMintableAmountMap.get(tokenHash).toString());
        }
        return null;
    }

    @Override
    public void putToMintableAmountMap(Hash tokenHash, BigDecimal amount) {
        currencyHashToMintableAmountMap.put(tokenHash, amount);
    }

    @Override
    public Hash getNativeCurrencyHash() {
        if (nativeCurrencyHash == null) {
            throw new CurrencyException("Native currency is missing.");
        }
        return nativeCurrencyHash;
    }

    @Override
    public boolean isCurrencyHashAllowed(Hash currencyHash) {
        return eventService.eventHappened(Event.MULTI_DAG) ||
                currencyHash == null;
    }

    @Override
    public void handleExistingTransaction(TransactionData transactionData) {
        handleTransaction(transactionData);
    }

    @Override
    public boolean isNativeCurrency(Hash currencyHash) {
        return getNativeCurrencyHash().equals(currencyHash) || currencyHash == null;
    }

    @Override
    public void handleMissingTransaction(TransactionData transactionData) {
        handleTransaction(transactionData);
        if (transactionData.getType().equals(TransactionType.TokenGeneration)) {
            TokenGenerationFeeBaseTransactionData tokenGenerationFeeData = transactionHelper.getTokenGenerationFeeData(transactionData);
            if (tokenGenerationFeeData != null) {
                OriginatorCurrencyData originatorCurrencyData = tokenGenerationFeeData.getServiceData().getOriginatorCurrencyData();
                Hash tokenHash = OriginatorCurrencyCrypto.calculateHash(originatorCurrencyData.getSymbol());
                Hash originatorHash = originatorCurrencyData.getOriginatorHash();
                addToUserCurrencyIndexes(originatorHash, tokenHash);
            }
        }
    }

    private void handleTransaction(TransactionData transactionData) {
        if (transactionData.getType().equals(TransactionType.TokenGeneration)) {
            TokenGenerationFeeBaseTransactionData tokenGenerationFeeBaseTransactionData;
            Optional<BaseTransactionData> optionalTokenGenerationFeeBaseTransactionData = transactionData.getBaseTransactions().stream().filter(TokenGenerationFeeBaseTransactionData.class::isInstance).findFirst();
            if (optionalTokenGenerationFeeBaseTransactionData.isPresent()) {
                tokenGenerationFeeBaseTransactionData = (TokenGenerationFeeBaseTransactionData) optionalTokenGenerationFeeBaseTransactionData.get();
                TokenGenerationServiceData tokenGenerationServiceData = tokenGenerationFeeBaseTransactionData.getServiceData();
                OriginatorCurrencyData originatorCurrencyData = tokenGenerationServiceData.getOriginatorCurrencyData();
                boolean transactionConfirmed = transactionHelper.isConfirmed(transactionData);
                CurrencyData currencyData = getCurrencyDataFromDB(originatorCurrencyData);

                if (currencyData == null) {
                    Instant createTime = tokenGenerationFeeBaseTransactionData.getCreateTime();
                    currencyData = getCurrencyDataInstance(tokenGenerationServiceData, createTime, originatorCurrencyData, transactionData);
                    if (transactionConfirmed && !isCurrencyNameUnique(currencyData.getHash(), currencyData.getName())) {
                        CurrencyNameIndexData previousCurrencyNameIndexData = currencyNameIndexes.getByHash(CryptoHelper.cryptoHash(currencyData.getName().getBytes(StandardCharsets.UTF_8)));
                        Hash previousCurrencyHash = previousCurrencyNameIndexData.getCurrencyHash();
                        removeUserCurrencyIndexByCurrencyHash(previousCurrencyHash);
                        currencies.deleteByHash(previousCurrencyHash);
                    }
                    if (isCurrencyNameUnique(currencyData.getHash(), currencyData.getName())) {
                        currencies.put(currencyData);
                        currencyNameIndexes.put(new CurrencyNameIndexData(currencyData.getName(), currencyData.getHash()));
                        addToUserCurrencyIndexes(currencyData.getOriginatorHash(), currencyData.getHash());
                        if (currencyData.isConfirmed()) {
                            initializeMintableAmountEntry(transactionData);
                        }
                    }
                } else if (currencyData.getCurrencyGeneratingTransactionHash().equals(transactionData.getHash())) {
                    if (transactionConfirmed && !currencyData.isConfirmed()) {
                        currencyData.setConfirmed(true);
                        currencies.put(currencyData);
                    }
                    if (currencyData.isConfirmed()) {
                        initializeMintableAmountEntry(transactionData);
                    }
                } else if (transactionConfirmed) {
                    Instant createTime = tokenGenerationFeeBaseTransactionData.getCreateTime();
                    CurrencyData newCurrencyData = getCurrencyDataInstance(tokenGenerationServiceData, createTime, originatorCurrencyData, transactionData);
                    currencies.put(newCurrencyData);
                    if (!newCurrencyData.getName().equals(currencyData.getName())) {
                        currencyNameIndexes.deleteByHash(CryptoHelper.cryptoHash(currencyData.getName().getBytes()));
                        currencyNameIndexes.put(new CurrencyNameIndexData(newCurrencyData.getName(), newCurrencyData.getHash()));
                    }
                    if (!currencyData.getOriginatorHash().equals(newCurrencyData.getOriginatorHash())) {
                        removeUserCurrencyIndexByCurrencyHash(currencyData.getHash());
                        addToUserCurrencyIndexes(newCurrencyData.getOriginatorHash(), newCurrencyData.getHash());
                    }
                    initializeMintableAmountEntry(transactionData);
                }
            }
        }
    }

    private void addToUserCurrencyIndexes(Hash originatorHash, Hash currencyHash) {
        UserCurrencyIndexData userCurrencyIndexData = userCurrencyIndexes.getByHash(originatorHash);
        if (userCurrencyIndexData == null) {
            Set<Hash> tokensHashSet = new HashSet<>();
            userCurrencyIndexData = new UserCurrencyIndexData(originatorHash, tokensHashSet);
        }
        userCurrencyIndexData.getTokenHashes().add(currencyHash);
        userCurrencyIndexes.put(userCurrencyIndexData);
    }

    private void removeFromUserCurrencyIndexes(Hash originatorHash, Hash currencyHash) {
        UserCurrencyIndexData userCurrencyIndexData = userCurrencyIndexes.getByHash(originatorHash);
        if (userCurrencyIndexData != null) {
            Set<Hash> tokenHashSet = userCurrencyIndexData.getTokenHashes();
            tokenHashSet.remove(currencyHash);
            if (tokenHashSet.isEmpty()) {
                userCurrencyIndexes.deleteByHash(originatorHash);
            }
        }
    }

    private void removeUserCurrencyIndexByCurrencyHash(Hash currencyHash) {
        CurrencyData currencyData = currencies.getByHash(currencyHash);
        if (currencyData != null) {
            Hash originatorHash = currencyData.getOriginatorHash();
            removeFromUserCurrencyIndexes(originatorHash, currencyHash);
        }
    }

    @Override
    public CurrencyData getCurrencyDataFromDB(OriginatorCurrencyData originatorCurrencyData) {

        Hash tokenHash = OriginatorCurrencyCrypto.calculateHash(originatorCurrencyData.getSymbol());
        return currencies.getByHash(tokenHash);
    }

    private CurrencyData getCurrencyDataInstance(TokenGenerationServiceData tokenGenerationServiceData, Instant createTime, OriginatorCurrencyData originatorCurrencyData, TransactionData transactionData) {
        CurrencyTypeData currencyTypeData = tokenGenerationServiceData.getCurrencyTypeData();
        Hash currencyGeneratingTransactionHash = transactionData.getHash();
        return new CurrencyData(originatorCurrencyData, currencyTypeData, createTime,
                currencyGeneratingTransactionHash, currencyGeneratingTransactionHash, transactionHelper.isConfirmed(transactionData));
    }

    private void initializeMintableAmountEntry(TransactionData transactionData) {
        TokenGenerationFeeBaseTransactionData tokenGenerationFeeData = transactionHelper.getTokenGenerationFeeData(transactionData);
        if (tokenGenerationFeeData != null) {
            Hash tokenHash = OriginatorCurrencyCrypto.calculateHash(tokenGenerationFeeData.getServiceData().getOriginatorCurrencyData().getSymbol());
            BigDecimal totalSupply = tokenGenerationFeeData.getServiceData().getOriginatorCurrencyData().getTotalSupply();
            BigDecimal mintableAmount = getTokenMintableAmount(tokenHash);
            if (mintableAmount == null) {
                putToMintableAmountMap(tokenHash, totalSupply);
                updateTokenHistory(tokenHash, transactionData);
                processPostponedMintingTransactions(tokenHash);
            } else {
                throw new CurrencyException(String.format("Attempting to generate existing token %s", tokenHash));
            }
        }
    }

    @Override
    public BigDecimal getPostponedMintingAmount(Hash tokenHash) {
        BigDecimal pendingMintingAmount = BigDecimal.ZERO;
        Set<TransactionData> setTransactionData = postponedTokenMintingTransactionsMap.get(tokenHash);
        if (setTransactionData != null) {
            return setTransactionData.stream().map(
                    transactionData -> {
                        TokenMintingFeeBaseTransactionData tokenMintingFeeData = transactionHelper.getTokenMintingFeeData(transactionData);
                        if (tokenMintingFeeData != null) {
                            return tokenMintingFeeData.getServiceData().getMintingAmount();
                        } else {
                            return BigDecimal.ZERO;
                        }
                    }
            ).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        return pendingMintingAmount;
    }

    @Override
    public void updateMintableAmountMapAndBalance(TransactionData transactionData) {
        TokenMintingFeeBaseTransactionData tokenMintingFeeData = transactionHelper.getTokenMintingFeeData(transactionData);
        if (tokenMintingFeeData != null) {
            Hash tokenHash = tokenMintingFeeData.getServiceData().getMintingCurrencyHash();
            BigDecimal mintingRequestedAmount = tokenMintingFeeData.getServiceData().getMintingAmount();
            Hash receiverAddress = tokenMintingFeeData.getServiceData().getReceiverAddress();
            BigDecimal mintableAmount = getTokenMintableAmount(tokenHash);
            boolean confirmed = transactionHelper.isConfirmed(transactionData);
            if (mintableAmount != null) {
                Boolean previousMintingTransactionConfirmed = mintingTransactionToConfirmationMap.get(transactionData.getHash());
                if (previousMintingTransactionConfirmed == null) {
                    putToMintableAmountMap(tokenHash, mintableAmount.subtract(mintingRequestedAmount));
                }
                if (previousMintingTransactionConfirmed == null && confirmed || previousMintingTransactionConfirmed != null && !previousMintingTransactionConfirmed.equals(confirmed)) {
                    balanceService.updateBalance(receiverAddress, tokenHash, mintingRequestedAmount);
                    balanceService.updatePreBalance(receiverAddress, tokenHash, mintingRequestedAmount);
                }
                mintingTransactionToConfirmationMap.put(transactionData.getHash(), confirmed);
                updateTokenHistory(tokenHash, transactionData);
            } else {
                postponedTokenMintingTransactionsMap.computeIfPresent(tokenHash, (hash, transactionSet) -> {
                    transactionSet.add(transactionData);
                    return transactionSet;
                });
                postponedTokenMintingTransactionsMap.putIfAbsent(tokenHash, new HashSet<>(Collections.singletonList(transactionData)));
            }
        }
    }

    @Override
    public void synchronizedUpdateMintableAmountMapAndBalance(TransactionData transactionData) {
        TokenMintingFeeBaseTransactionData tokenMintingFeeData = transactionHelper.getTokenMintingFeeData(transactionData);
        if (tokenMintingFeeData != null) {
            Hash currencyHash = tokenMintingFeeData.getServiceData().getMintingCurrencyHash();
            try {
                synchronized (mintingTokenHashLockData.addLockToLockMap(currencyHash)) {
                    updateMintableAmountMapAndBalance(transactionData);
                }
            } finally {
                mintingTokenHashLockData.removeLockFromLocksMap(currencyHash);
            }
        }
    }

    protected void validateCurrencyUniqueness(Hash currencyHash, String currencyName) {
        if (!isCurrencyNameUnique(currencyHash, currencyName)) {
            throw new CurrencyException("Currency name is already in use.");
        }
        if (!isCurrencySymbolUnique(currencyHash)) {
            throw new CurrencyException("Currency symbol is already in use.");
        }
    }

    private boolean isCurrencyNameUnique(Hash currencyHash, String currencyName) {
        return !currencyName.equalsIgnoreCase(nativeCurrencyName) &&
                currencyNameIndexes.getByHash(new CurrencyNameIndexData(currencyName, currencyHash).getHash()) == null;
    }

    private boolean isCurrencySymbolUnique(Hash currencyHash) {
        return !currencyHash.equals(nativeCurrencyHash) && currencies.getByHash(currencyHash) == null;
    }

    @Override
    public boolean validateCurrencyUniquenessAndAddUnconfirmedRecord(TransactionData transactionData) {
        TokenGenerationFeeBaseTransactionData tokenGenerationFeeBaseTransactionData = transactionHelper.getTokenGenerationFeeData(transactionData);

        if (!transactionHelper.validateBaseTransactionPublicKey(tokenGenerationFeeBaseTransactionData, NodeType.FinancialServer)) {
            log.error("Error in generation check. Base transaction not signed by an authorized financial server");
            return false;
        }

        OriginatorCurrencyData originatorCurrencyData = tokenGenerationFeeBaseTransactionData.getServiceData().getOriginatorCurrencyData();
        CurrencyTypeData currencyTypeData = tokenGenerationFeeBaseTransactionData.getServiceData().getCurrencyTypeData();
        Hash currencyHash = OriginatorCurrencyCrypto.calculateHash(originatorCurrencyData.getSymbol());
        Hash currencyNameHash = CryptoHelper.cryptoHash(originatorCurrencyData.getName().getBytes());
        try {
            synchronized (currencyLockData.addLockToLockMap(currencyHash)) {
                synchronized (currencyNameLockData.addLockToLockMap(currencyNameHash)) {
                    if (!isCurrencyNameUnique(currencyHash, originatorCurrencyData.getName()) || !isCurrencySymbolUnique(currencyHash)) {
                        return false;
                    }
                    CurrencyData currencyData = new CurrencyData(originatorCurrencyData, currencyTypeData, tokenGenerationFeeBaseTransactionData.getCreateTime(),
                            transactionData.getHash(), transactionData.getHash(), false);
                    currencies.put(currencyData);
                    currencyNameIndexes.put(new CurrencyNameIndexData(currencyData.getName(), currencyHash));
                    addToUserCurrencyIndexes(currencyData.getOriginatorHash(), currencyHash);
                    return true;
                }
            }
        } finally {
            currencyNameLockData.removeLockFromLocksMap(currencyNameHash);
            currencyLockData.removeLockFromLocksMap(currencyHash);
        }
    }

    public void addConfirmedCurrency(TransactionData transactionData) {
        TokenGenerationFeeBaseTransactionData tokenGenerationFeeBaseTransactionData = transactionHelper.getTokenGenerationFeeData(transactionData);
        OriginatorCurrencyData originatorCurrencyData = tokenGenerationFeeBaseTransactionData.getServiceData().getOriginatorCurrencyData();
        Hash originatorHash = originatorCurrencyData.getOriginatorHash();
        Hash currencyHash = OriginatorCurrencyCrypto.calculateHash(originatorCurrencyData.getSymbol());
        try {
            synchronized (currencyLockData.addLockToLockMap(currencyHash)) {
                CurrencyData currencyData = currencies.getByHash(currencyHash);
                if (currencyData == null) {
                    CurrencyTypeData currencyTypeData = tokenGenerationFeeBaseTransactionData.getServiceData().getCurrencyTypeData();
                    currencyData = new CurrencyData(originatorCurrencyData, currencyTypeData, transactionData.getCreateTime(),
                            transactionData.getHash(), transactionData.getHash(), true);
                } else {
                    currencyData.setConfirmed(true);
                }
                currencies.put(currencyData);
                putToMintableAmountMap(currencyHash, originatorCurrencyData.getTotalSupply());
                processPostponedMintingTransactions(currencyHash);
                updateTokenHistory(currencyHash, transactionData);
                try {
                    synchronized (originatorHashLockData.addLockToLockMap(originatorHash)) {
                        addToUserCurrencyIndexes(originatorHash, currencyHash);
                    }
                } finally {
                    originatorHashLockData.removeLockFromLocksMap(originatorHash);
                }
            }
        } finally {
            currencyLockData.removeLockFromLocksMap(currencyHash);
        }
    }

    private void processPostponedMintingTransactions(Hash currencyHash) {
        try {
            synchronized (mintingTokenHashLockData.addLockToLockMap(currencyHash)) {
                Set<TransactionData> setTransactionData = postponedTokenMintingTransactionsMap.get(currencyHash);
                if (setTransactionData != null) {
                    setTransactionData.forEach(this::updateMintableAmountMapAndBalance);
                    postponedTokenMintingTransactionsMap.remove(currencyHash);
                }
            }
        } finally {
            mintingTokenHashLockData.removeLockFromLocksMap(currencyHash);
        }
    }

    public ResponseEntity<IResponse> getUserTokens(GetUserTokensRequest getUserTokensRequest) {
        try {
            if (!eventService.eventHappened(Event.MULTI_DAG)) {
                return ResponseEntity.badRequest().body(new Response(MULTI_DAG_IS_NOT_SUPPORTED, STATUS_ERROR));
            }
            if (!getUserTokensRequestCrypto.verifySignature(getUserTokensRequest)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
            }
            UserCurrencyIndexData userCurrencyIndexData = userCurrencyIndexes.getByHash(getUserTokensRequest.getUserHash());
            GetUserTokensResponse getUserTokensResponse = new GetUserTokensResponse();
            if (userCurrencyIndexData == null) {
                return ResponseEntity.ok(getUserTokensResponse);
            }
            Set<TokenResponseData> userTokens = new HashSet<>();
            Set<Hash> tokenHashes = userCurrencyIndexData.getTokenHashes();
            tokenHashes.forEach(tokenHash ->
                    userTokens.add(fillTokenGenerationResponseData(tokenHash)));
            getUserTokensResponse.setUserTokens(userTokens);
            return ResponseEntity.ok(getUserTokensResponse);
        } catch (Exception e) {
            log.error(ERROR_AT_GETTING_USER_TOKENS + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    public ResponseEntity<IResponse> getTokenDetails(GetTokenDetailsRequest getTokenDetailsRequest) {
        try {
            if (!eventService.eventHappened(Event.MULTI_DAG)) {
                return ResponseEntity.badRequest().body(new Response(MULTI_DAG_IS_NOT_SUPPORTED, STATUS_ERROR));
            }
            Hash currencyHash = getTokenDetailsRequest.getCurrencyHash();
            return getTokenDetails(currencyHash);
        } catch (Exception e) {
            log.error(ERROR_AT_GETTING_USER_TOKENS + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    public ResponseEntity<IResponse> getTokenSymbolDetails(GetTokenSymbolDetailsRequest getTokenSymbolDetailsRequest) {
        try {
            if (!eventService.eventHappened(Event.MULTI_DAG)) {
                return ResponseEntity.badRequest().body(new Response(MULTI_DAG_IS_NOT_SUPPORTED, STATUS_ERROR));
            }
            Hash currencyHash = OriginatorCurrencyCrypto.calculateHash(getTokenSymbolDetailsRequest.getSymbol());
            return getTokenDetails(currencyHash);
        } catch (Exception e) {
            log.error(ERROR_AT_GETTING_USER_TOKENS + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    private ResponseEntity<IResponse> getTokenHistory(Hash currencyHash) {
        Set<TransactionResponseData> transactionSet = new HashSet<>();
        tokenTransactionHashesMap.get(currencyHash).forEach(transactionHash -> {
            TransactionData transactionData = transactions.getByHash(transactionHash);
            transactionSet.add(new TransactionResponseData(transactionData));
        });
        GetTokenHistoryResponse getTokenHistoryResponse = new GetTokenHistoryResponse(transactionSet);
        return ResponseEntity.ok(getTokenHistoryResponse);
    }

    private ResponseEntity<IResponse> getTokenDetails(Hash currencyHash) {
        GetTokenDetailsResponse getTokenDetailsResponse = new GetTokenDetailsResponse();
        TokenResponseData tokenResponseData = fillTokenGenerationResponseData(currencyHash);
        getTokenDetailsResponse.setToken(tokenResponseData);
        return ResponseEntity.ok(getTokenDetailsResponse);
    }

    public TokenResponseData fillTokenGenerationResponseData(Hash currencyHash) {
        CurrencyData currencyData = currencies.getByHash(currencyHash);
        if (currencyData == null) {
            throw new CurrencyException(String.format("Unidentified currency hash: %s", currencyHash));
        }
        TokenResponseData tokenResponseData = new TokenResponseData(currencyData);

        BigDecimal mintableAmount = getTokenMintableAmount(currencyHash);
        BigDecimal alreadyMintedAmount = BigDecimal.ZERO;
        if (mintableAmount == null) {
            mintableAmount = BigDecimal.ZERO;
        } else {
            alreadyMintedAmount = currencyData.getTotalSupply().subtract(mintableAmount);
        }
        tokenResponseData.setMintedAmount(alreadyMintedAmount);
        tokenResponseData.setMintableAmount(mintableAmount);
        return tokenResponseData;
    }

    @Override
    public ResponseEntity<IResponse> getTokenHistory(GetTokenHistoryRequest getTokenHistoryRequest) {
        try {
            if (!eventService.eventHappened(Event.MULTI_DAG)) {
                return ResponseEntity.badRequest().body(new Response(MULTI_DAG_IS_NOT_SUPPORTED, STATUS_ERROR));
            }
            Hash currencyHash = getTokenHistoryRequest.getCurrencyHash();
            return getTokenHistory(currencyHash);
        } catch (Exception e) {
            log.error(ERROR_AT_GETTING_USER_TOKENS + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    public Hash getNativeCurrencyHashIfNull(Hash currencyHash) {
        return Optional.ofNullable(currencyHash).orElse(getNativeCurrencyHash());
    }

    private void updateTokenHistory(Hash currencyHash, TransactionData transactionData) {

        tokenTransactionHashesMap.computeIfPresent(currencyHash, (hash, transactionSet) -> {
            transactionSet.add(transactionData.getHash());
            return transactionSet;
        });
        tokenTransactionHashesMap.putIfAbsent(currencyHash, new HashSet<>(Collections.singletonList(transactionData.getHash())));

    }

}
