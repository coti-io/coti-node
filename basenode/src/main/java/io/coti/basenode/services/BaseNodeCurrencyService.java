package io.coti.basenode.services;

import com.google.gson.Gson;
import io.coti.basenode.crypto.*;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.CurrencyException;
import io.coti.basenode.exceptions.CurrencyValidationException;
import io.coti.basenode.http.*;
import io.coti.basenode.http.data.TokenGenerationResponseData;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.model.CurrencyNameIndexes;
import io.coti.basenode.model.UserCurrencyIndexes;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.ICurrencyService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;

@Slf4j
@Service
public class BaseNodeCurrencyService implements ICurrencyService {

    public static final String ERROR_AT_GETTING_USER_TOKENS = "Error at getting user tokens: ";
    private static final String RECOVERY_NODE_GET_NATIVE_CURRENCY_ENDPOINT = "/currencies/recoverNative";
    protected CurrencyData nativeCurrencyData;
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
    private OriginatorCurrencyCrypto originatorCurrencyCrypto;
    @Autowired
    private GetUserTokensRequestCrypto getUserTokensRequestCrypto;
    @Autowired
    private GetTokenDetailsRequestCrypto getTokenDetailsRequestCrypto;
    @Autowired
    private GetTokenSymbolDetailsRequestCrypto getTokenSymbolDetailsRequestCrypto;
    @Autowired
    protected IBalanceService balanceService;
    @Autowired
    private UserCurrencyIndexes userCurrencyIndexes;
    @Autowired
    private ITransactionHelper transactionHelper;
    private final LockData currencyLockData = new LockData();
    private final LockData currencyNameLockData = new LockData();
    private final LockData originatorHashLockData = new LockData();
    private Map<Hash, BigDecimal> currencyHashToMintableAmountMap;
    private Map<Hash, Set<TransactionData>> postponedTokenMintingTransactionsMap;
    private Map<Hash, Boolean> mintingTransactionToConfirmationMap;

    public void init() {
        currencyHashToMintableAmountMap = new ConcurrentHashMap<>();
        postponedTokenMintingTransactionsMap = new ConcurrentHashMap<>();
        mintingTransactionToConfirmationMap = new ConcurrentHashMap<>();
        updateCurrencies();
        try {
            nativeCurrencyData = null;
            setNativeCurrencyFromExistingCurrencies();
            log.info("{} is up", this.getClass().getSimpleName());
        } catch (CurrencyException e) {
            throw new CurrencyException("Error at currency service init.\n" + e.getMessage(), e);
        } catch (Exception e) {
            throw new CurrencyException("Error at currency service init.", e);
        }
    }

    private void setNativeCurrencyDataFromRecoveryServer() {
        if (networkService.getRecoveryServerAddress() != null) {
            try {
                GetNativeCurrencyResponse getNativeCurrencyResponse = restTemplate.getForObject(networkService.getRecoveryServerAddress() + RECOVERY_NODE_GET_NATIVE_CURRENCY_ENDPOINT, GetNativeCurrencyResponse.class);
                if (getNativeCurrencyResponse != null) {
                    CurrencyData recoveredNativeCurrency = getNativeCurrencyResponse.getNativeCurrency();
                    if (recoveredNativeCurrency != null && recoveredNativeCurrency.getCurrencyTypeData().getCurrencyType().equals(CurrencyType.NATIVE_COIN)) {
                        currencies.put(recoveredNativeCurrency);
                    }
                }
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                throw new CurrencyException(String.format("Get native currency from restore node error. Recovery node response: %s", new Gson().fromJson(e.getResponseBodyAsString(), Response.class).getMessage()), e);
            } catch (Exception e) {
                throw new CurrencyException("Attempted to override existing native currency", e);
            }
        }
    }

    @Override
    public void updateCurrencies() {
        setNativeCurrencyDataFromRecoveryServer();
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

    private void setNativeCurrencyFromExistingCurrencies() {
        if (!currencies.isEmpty()) {
            currencies.forEach(currencyData -> {
                if (currencyData.getCurrencyTypeData().getCurrencyType().equals(CurrencyType.NATIVE_COIN)) {
                    verifyNativeCurrency(currencyData);
                }
            });
        }
    }

    protected void setNativeCurrencyData(CurrencyData currencyData) {
        if (this.nativeCurrencyData != null) {
            throw new CurrencyException("Attempted to override existing native currency");
        }
        this.nativeCurrencyData = currencyData;
    }

    @Override
    public CurrencyData getNativeCurrency() {
        return this.nativeCurrencyData;
    }

    @Override
    public Hash getNativeCurrencyHash() {
        if (nativeCurrencyData == null) {
            throw new CurrencyException("Native currency is missing.");
        }
        return nativeCurrencyData.getHash();
    }

    @Override
    public void generateNativeCurrency() {
        throw new CurrencyException("Attempted to generate Native currency.");
    }

    private void verifyNativeCurrency(CurrencyData nativeCurrency) {
        if (nativeCurrency == null) {
            throw new CurrencyException("Failed to verify native currency data exists");
        }
        if (!originatorCurrencyCrypto.verifySignature(nativeCurrency)) {
            throw new CurrencyException("Failed to verify native currency data of " + nativeCurrency.getHash());
        } else {
            CurrencyTypeData nativeCurrencyTypeData = nativeCurrency.getCurrencyTypeData();
            CurrencyTypeRegistrationData currencyTypeRegistrationData = new CurrencyTypeRegistrationData(nativeCurrency.getSymbol(), nativeCurrencyTypeData);
            if (!currencyTypeRegistrationCrypto.verifySignature(currencyTypeRegistrationData)) {
                throw new CurrencyException("Failed to verify native currency data type of " + nativeCurrency.getCurrencyTypeData().getCurrencyType().getText());
            }
        }
        if (currencies.getByHash(nativeCurrency.getHash()) == null) {
            currencies.put(nativeCurrency);
        }
        if (this.nativeCurrencyData == null) {
            setNativeCurrencyData(nativeCurrency);
        }
    }

    @Override
    public void handleExistingTransaction(TransactionData transactionData) {
        handleTransaction(transactionData);
    }

    @Override
    public ResponseEntity<IResponse> getNativeCurrencyResponse() {
        CurrencyData nativeCurrency = getNativeCurrency();
        GetNativeCurrencyResponse getNativeCurrencyResponse = new GetNativeCurrencyResponse();

        if (nativeCurrency != null) {
            getNativeCurrencyResponse.setNativeCurrency(nativeCurrency);
            return ResponseEntity.ok(getNativeCurrencyResponse);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response("Native currency not found", STATUS_ERROR));
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
            Optional<BaseTransactionData> firstBaseTransactionData = transactionData.getBaseTransactions().stream().filter(baseTransactionData -> baseTransactionData instanceof TokenGenerationFeeBaseTransactionData).findFirst();
            if (firstBaseTransactionData.isPresent()) {
                tokenGenerationFeeBaseTransactionData = (TokenGenerationFeeBaseTransactionData) firstBaseTransactionData.get();
                TokenGenerationData tokenGenerationData = tokenGenerationFeeBaseTransactionData.getServiceData();
                OriginatorCurrencyData originatorCurrencyData = tokenGenerationData.getOriginatorCurrencyData();
                boolean transactionConfirmed = transactionHelper.isConfirmed(transactionData);
                CurrencyData currencyData = getCurrencyDataFromDB(originatorCurrencyData);

                if (currencyData == null) {
                    Instant createTime = tokenGenerationFeeBaseTransactionData.getCreateTime();
                    currencyData = getCurrencyDataInstance(tokenGenerationData, createTime, originatorCurrencyData, transactionData);
                    if (transactionConfirmed && !isCurrencyNameUnique(currencyData.getHash(), currencyData.getName())) {
                        CurrencyNameIndexData previousCurrencyNameIndexData = currencyNameIndexes.getByHash(CryptoHelper.cryptoHash(currencyData.getName().getBytes()));
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
                    CurrencyData newCurrencyData = getCurrencyDataInstance(tokenGenerationData, createTime, originatorCurrencyData, transactionData);
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

    private CurrencyData getCurrencyDataInstance(TokenGenerationData tokenGenerationData, Instant createTime, OriginatorCurrencyData originatorCurrencyData, TransactionData transactionData) {
        CurrencyTypeData currencyTypeData = tokenGenerationData.getCurrencyTypeData();
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
                Set<TransactionData> setTransactionData = postponedTokenMintingTransactionsMap.get(tokenHash);
                if (setTransactionData != null) {
                    setTransactionData.forEach(this::updateMintableAmountMapAndBalance);
                    postponedTokenMintingTransactionsMap.remove(tokenHash);
                }
            } else {
                throw new CurrencyException(String.format("Attempting to generate existing token %s", tokenHash));
            }
        }
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
    public void putCurrencyData(CurrencyData currencyData) {
        if (currencyData == null) {
            throw new CurrencyException("Failed to add an empty currency");
        }
        currencies.put(currencyData);
        updateCurrencyNameIndex(currencyData);
    }

    private void updateCurrencyNameIndex(CurrencyData currencyData) {
        currencyNameIndexes.put(new CurrencyNameIndexData(currencyData.getName(), currencyData.getHash()));
    }

    @Override
    public void validateName(OriginatorCurrencyData originatorCurrencyData) {
        String name = originatorCurrencyData.getName();
        if (name.length() != name.trim().length()) {
            throw new CurrencyValidationException(String.format("Attempted to set an invalid currency name with spaces at the start or the end %s.", name));
        }
        final String[] words = name.split(" ");
        for (String word : words) {
            if (word == null || word.isEmpty() || !Pattern.compile("[A-Za-z0-9.]+").matcher(word).matches()) {
                throw new CurrencyValidationException(String.format("Attempted to set an invalid currency name with the word %s.", name));
            }
        }
    }

    @Override
    public void validateSymbol(OriginatorCurrencyData originatorCurrencyData) {
        String symbol = originatorCurrencyData.getSymbol();
        if (!Pattern.compile("[A-Z.]{0,15}").matcher(symbol).matches()) {
            throw new CurrencyException(String.format("Attempted to set an invalid currency symbol of %s.", symbol));
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
        return currencyNameIndexes.getByHash(new CurrencyNameIndexData(currencyName, currencyHash).getHash()) == null;
    }

    private boolean isCurrencySymbolUnique(Hash currencyHash) {
        return currencies.getByHash(currencyHash) == null;
    }

    @Override
    public boolean validateCurrencyUniquenessAndAddUnconfirmedRecord(TransactionData transactionData) {
        TokenGenerationFeeBaseTransactionData tokenGenerationFeeBaseTransactionData = transactionHelper.getTokenGenerationFeeData(transactionData);
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

    public ResponseEntity<IResponse> getUserTokens(GetUserTokensRequest getUserTokensRequest) {
        try {
            if (!getUserTokensRequestCrypto.verifySignature(getUserTokensRequest)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
            }
            UserCurrencyIndexData userCurrencyIndexData = userCurrencyIndexes.getByHash(getUserTokensRequest.getUserHash());
            GetUserTokensResponse getUserTokensResponse = new GetUserTokensResponse();
            if (userCurrencyIndexData == null) {
                return ResponseEntity.ok(getUserTokensResponse);
            }
            Set<TokenGenerationResponseData> userTokens = new HashSet<>();
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
            if (!getTokenDetailsRequestCrypto.verifySignature(getTokenDetailsRequest)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
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
            if (!getTokenSymbolDetailsRequestCrypto.verifySignature(getTokenSymbolDetailsRequest)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(INVALID_SIGNATURE, STATUS_ERROR));
            }
            Hash currencyHash = OriginatorCurrencyCrypto.calculateHash(getTokenSymbolDetailsRequest.getSymbol());
            return getTokenDetails(currencyHash);
        } catch (Exception e) {
            log.error(ERROR_AT_GETTING_USER_TOKENS + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    private ResponseEntity<IResponse> getTokenDetails(Hash currencyHash) {
        GetTokenDetailsResponse getTokenDetailsResponse = new GetTokenDetailsResponse();
        TokenGenerationResponseData tokenGenerationResponseData = fillTokenGenerationResponseData(currencyHash);
        getTokenDetailsResponse.setToken(tokenGenerationResponseData);
        return ResponseEntity.ok(getTokenDetailsResponse);
    }

    private TokenGenerationResponseData fillTokenGenerationResponseData(Hash currencyHash) {
        CurrencyData currencyData = currencies.getByHash(currencyHash);
        if (currencyData == null) {
            throw new CurrencyException(String.format("Unidentified currency hash: %s", currencyHash));
        }
        TokenGenerationResponseData tokenGenerationResponseData = new TokenGenerationResponseData(currencyData);

        BigDecimal mintableAmount = getTokenMintableAmount(currencyHash);
        BigDecimal alreadyMintedAmount = BigDecimal.ZERO;
        if (mintableAmount == null) {
            mintableAmount = BigDecimal.ZERO;
        } else {
            alreadyMintedAmount = currencyData.getTotalSupply().subtract(mintableAmount);
        }
        tokenGenerationResponseData.setMintedAmount(alreadyMintedAmount);
        tokenGenerationResponseData.setMintableAmount(mintableAmount);
        return tokenGenerationResponseData;
    }


}
