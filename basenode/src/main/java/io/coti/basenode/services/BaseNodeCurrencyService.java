package io.coti.basenode.services;

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
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_SIGNATURE;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;

@Slf4j
@Service
public class BaseNodeCurrencyService implements ICurrencyService {

    public static final String ERROR_AT_GETTING_USER_TOKENS = "Error at getting user tokens: ";
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
    private Map<Hash, Hash> lockHashMap = new ConcurrentHashMap<>();
    private final Object lock = new Object();
    private Map<Hash, BigDecimal> currencyHashToMintableAmountMap;

    public void init() {
        currencyHashToMintableAmountMap = new ConcurrentHashMap<>();
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

    @Override
    public void updateCurrenciesFromClusterStamp(Map<Hash, CurrencyData> clusterStampCurrenciesMap) {
        clusterStampCurrenciesMap.forEach((currencyHash, clusterStampCurrencyData) -> {
                    currencies.put(clusterStampCurrencyData);
                    if (clusterStampCurrencyData.getCurrencyTypeData().getCurrencyType().equals(CurrencyType.NATIVE_COIN)) {
                        verifyNativeCurrency(clusterStampCurrencyData);
                    }
                }
        );
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
        TransactionType transactionType = transactionData.getType();
        if (transactionType.equals(TransactionType.TokenGeneration)) {
            updateMintableAmountMap(transactionData);
        }
    }

    private void updateMintableAmountMap(TransactionData transactionData) {
        TokenGenerationFeeBaseTransactionData tokenGenerationFeeData = getTokenGenerationFeeData(transactionData);
        if (tokenGenerationFeeData != null) {
            Hash tokenHash = OriginatorCurrencyCrypto.calculateHash(tokenGenerationFeeData.getServiceData().getOriginatorCurrencyData().getSymbol());
            BigDecimal totalSupply = tokenGenerationFeeData.getServiceData().getOriginatorCurrencyData().getTotalSupply();
            BigDecimal mintableAmount = getTokenMintableAmount(tokenHash);
            if (mintableAmount != null) {
                putToMintableAmountMap(tokenHash, mintableAmount.add(totalSupply));
            } else {
                putToMintableAmountMap(tokenHash, totalSupply);
            }
        }
    }

    @Override
    public void handleMissingTransaction(TransactionData transactionData) {
        boolean dspConsensus = transactionData.getDspConsensusResult().isDspConsensus();
        if (transactionData.getType().equals(TransactionType.TokenGeneration)) {
            CurrencyData currencyData = getCurrencyData(transactionData);
            if (currencyData != null) {
                if (dspConsensus) {
                    currencyData.setConfirmed(true);
                }
                currencies.put(currencyData);
                updateMintableAmountMap(transactionData);
            }
        }
    }

    @Override
    public CurrencyData getCurrencyData(TransactionData transactionData) {
        CurrencyData currencyData = null;
        Optional<BaseTransactionData> firstBaseTransactionData = transactionData.getBaseTransactions().stream().filter(baseTransactionData -> baseTransactionData instanceof TokenGenerationFeeBaseTransactionData).findFirst();
        if (firstBaseTransactionData.isPresent()) {
            TokenGenerationFeeBaseTransactionData tokenGenerationFeeBaseTransactionData = (TokenGenerationFeeBaseTransactionData) firstBaseTransactionData.get();
            TokenGenerationData tokenGenerationData = tokenGenerationFeeBaseTransactionData.getServiceData();
            Hash currencyLastTypeChangingTransactionHash = transactionData.getHash();
            OriginatorCurrencyData originatorCurrencyData = tokenGenerationData.getOriginatorCurrencyData();
            CurrencyTypeData currencyTypeData = tokenGenerationData.getCurrencyTypeData();
            Instant createTime = tokenGenerationFeeBaseTransactionData.getCreateTime();
            Hash currencyGeneratingTransactionHash = transactionData.getHash();
            currencyData = new CurrencyData(originatorCurrencyData, currencyTypeData, createTime,
                    currencyGeneratingTransactionHash, currencyLastTypeChangingTransactionHash, transactionHelper.isConfirmed(transactionData));
            currencyData.setHash();
        }
        return currencyData;
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

    protected void validateCurrencyUniqueness(Hash currencyHash, String currencyName) {
        if (currencyNameIndexes.getByHash(new CurrencyNameIndexData(currencyName, currencyHash).getHash()) != null) {
            throw new CurrencyException("Currency name is already in use.");
        }
        CurrencyData currencyData = currencies.getByHash(currencyHash);
        if (currencyData != null) {
            throw new CurrencyException("Currency symbol is already in use.");
        }
    }

    @Override
    public void validateName(OriginatorCurrencyData originatorCurrencyData) {
        String name = originatorCurrencyData.getName();
        if (name.length() != name.trim().length()) {
            throw new CurrencyValidationException(String.format("Attempted to set an invalid currency name with spaces at the start or the end %s.", name));
        }
        final String[] words = name.split(" ");
        for (String word : words) {
            if (word == null || word.isEmpty() || !Pattern.compile("[A-Za-z0-9]+").matcher(word).matches()) {
                throw new CurrencyValidationException(String.format("Attempted to set an invalid currency name with the word %s.", name));
            }
        }
    }

    @Override
    public void validateSymbol(OriginatorCurrencyData originatorCurrencyData) {
        String symbol = originatorCurrencyData.getSymbol();
        if (!Pattern.compile("[A-Z]{0,15}").matcher(symbol).matches()) {
            throw new CurrencyException(String.format("Attempted to set an invalid currency symbol of %s.", symbol));
        }
    }

    @Override
    public boolean validateCurrencyUniquenessAndAddUnconfirmedRecord(TransactionData transactionData) {
        TokenGenerationFeeBaseTransactionData tokenGenerationFeeBaseTransactionData = getTokenGenerationFeeData(transactionData);
        OriginatorCurrencyData originatorCurrencyData = tokenGenerationFeeBaseTransactionData.getServiceData().getOriginatorCurrencyData();
        CurrencyTypeData currencyTypeData = tokenGenerationFeeBaseTransactionData.getServiceData().getCurrencyTypeData();
        Hash currencyHash = OriginatorCurrencyCrypto.calculateHash(originatorCurrencyData.getSymbol());
        try {
            synchronized (addLockToLockMap(currencyHash)) {
                if (currencyNameIndexes.getByHash(new CurrencyNameIndexData(originatorCurrencyData.getName(), currencyHash).getHash()) != null) {
                    return false;
                }
                if (currencies.getByHash(currencyHash) != null) {
                    return false;
                }
                CurrencyData currencyData = new CurrencyData(originatorCurrencyData, currencyTypeData, transactionData.getCreateTime(),
                        transactionData.getHash(), transactionData.getHash(), false);
                currencies.put(currencyData);
                // transactionData.getCreateTime  not the time from dspconsensus, because the record should be the same time in all nodes.
                return true;
            }
        } finally {
            removeLockFromLocksMap(currencyHash);
        }
    }

    public void addConfirmedCurrency(TransactionData transactionData) {
        TokenGenerationFeeBaseTransactionData tokenGenerationFeeBaseTransactionData = getTokenGenerationFeeData(transactionData);
        OriginatorCurrencyData originatorCurrencyData = tokenGenerationFeeBaseTransactionData.getServiceData().getOriginatorCurrencyData();
        Hash originatorHash = originatorCurrencyData.getOriginatorHash();
        Hash currencyHash = OriginatorCurrencyCrypto.calculateHash(originatorCurrencyData.getSymbol());
        try {
            synchronized (addLockToLockMap(currencyHash)) {
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
                    synchronized (addLockToLockMap(originatorHash)) {
                        UserCurrencyIndexData userCurrencyIndexData = userCurrencyIndexes.getByHash(originatorHash);
                        if (userCurrencyIndexData == null) {
                            HashSet<Hash> tokensHashSet = new HashSet<>();
                            tokensHashSet.add(currencyHash);
                            userCurrencyIndexes.put(new UserCurrencyIndexData(originatorCurrencyData.getOriginatorHash(), tokensHashSet));
                        } else {
                            userCurrencyIndexData.getTokenHashes().add(currencyHash);
                            userCurrencyIndexes.put(userCurrencyIndexData);
                        }
                    }
                } finally {
                    removeLockFromLocksMap(originatorHash);
                }
            }
        } finally {
            removeLockFromLocksMap(currencyHash);
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


    protected TokenGenerationFeeBaseTransactionData getTokenGenerationFeeData(TransactionData tokenGenerationTransaction) {
        return (TokenGenerationFeeBaseTransactionData) tokenGenerationTransaction
                .getBaseTransactions()
                .stream()
                .filter(t -> t instanceof TokenGenerationFeeBaseTransactionData)
                .findFirst().orElse(null);
    }

    private Hash addLockToLockMap(Hash hash) {
        synchronized (lock) {
            lockHashMap.putIfAbsent(hash, hash);   // use the same map for two locks, it is ok, these hashes are even of different lengths
            return lockHashMap.get(hash);
        }
    }

    private void removeLockFromLocksMap(Hash hash) {
        synchronized (lock) {
            lockHashMap.remove(hash);
        }
    }
}
