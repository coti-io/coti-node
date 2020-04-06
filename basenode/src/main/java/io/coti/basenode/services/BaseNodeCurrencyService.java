package io.coti.basenode.services;

import io.coti.basenode.crypto.CurrencyTypeRegistrationCrypto;
import io.coti.basenode.crypto.GetUpdatedCurrencyRequestCrypto;
import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.CurrencyException;
import io.coti.basenode.http.GetUpdatedCurrencyRequest;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.model.CurrencyNameIndexes;
import io.coti.basenode.model.UserTokenGenerations;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.ICurrencyService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.FluxSink;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class BaseNodeCurrencyService implements ICurrencyService {

    private static final int NUMBER_OF_NATIVE_CURRENCY = 1;
    private EnumMap<CurrencyType, HashSet<Hash>> currencyHashByTypeMap;
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
    private GetUpdatedCurrencyRequestCrypto getUpdatedCurrencyRequestCrypto;
    @Autowired
    protected CurrencyTypeRegistrationCrypto currencyTypeRegistrationCrypto;
    @Autowired
    private OriginatorCurrencyCrypto originatorCurrencyCrypto;
    @Autowired
    protected IBalanceService balanceService;
    @Autowired
    private UserTokenGenerations userTokenGenerations;
    @Autowired
    private ITransactionHelper transactionHelper;
    private Map<Hash, Hash> lockHashMap = new ConcurrentHashMap<>();
    private final Object lock = new Object();

    public void init() {
        try {
            currencyHashByTypeMap = new EnumMap<>(CurrencyType.class);
            nativeCurrencyData = null;
            updateCurrencyHashByTypeMapFromExistingCurrencies();
            updateCurrencies();
            log.info("{} is up", this.getClass().getSimpleName());
        } catch (CurrencyException e) {
            throw new CurrencyException("Error at currency service init.\n" + e.getMessage(), e);
        } catch (Exception e) {
            throw new CurrencyException("Error at currency service init.", e);
        }
    }

    public void updateCurrencyHashByTypeMapFromExistingCurrencies() {
        currencies.forEach(this::updateCurrencyHashByTypeMap);
        if (!currencies.isEmpty()) {
            verifyValidNativeCurrencyPresent();
        }
    }

    protected void updateCurrencyHashByTypeMap(CurrencyData currencyData) {
        CurrencyType currencyType = currencyData.getCurrencyTypeData().getCurrencyType();
        currencyHashByTypeMap.putIfAbsent(currencyType, new HashSet<>());
        currencyHashByTypeMap.get(currencyType).add(currencyData.getHash());
    }

    protected HashSet getCurrencyHashesByCurrencyType(CurrencyType currencyType) {
        return currencyHashByTypeMap.get(currencyType);
    }

    @Override
    public void updateCurrencies() {
    }

    protected void replaceExistingCurrencyDataDueToTypeChange(CurrencyData originalCurrencyData, CurrencyData recoveredCurrencyData) {
        CurrencyType originalCurrencyType = originalCurrencyData.getCurrencyTypeData().getCurrencyType();
        currencies.delete(originalCurrencyData);
        currencies.put(recoveredCurrencyData);
        currencyHashByTypeMap.get(originalCurrencyType).remove(originalCurrencyData.getHash());
        updateCurrencyHashByTypeMap(recoveredCurrencyData);
    }

    private void verifyValidNativeCurrencyPresent() {
        HashSet<Hash> nativeCurrencyHashes = currencyHashByTypeMap.get(CurrencyType.NATIVE_COIN);
        if (nativeCurrencyHashes == null || nativeCurrencyHashes.isEmpty() || nativeCurrencyHashes.size() != NUMBER_OF_NATIVE_CURRENCY) {
            throw new CurrencyException("Failed to retrieve native currency data");
        } else {
            Hash nativeCurrencyHash = nativeCurrencyHashes.iterator().next();
            CurrencyData nativeCurrency = currencies.getByHash(nativeCurrencyHash);
            if (!originatorCurrencyCrypto.verifySignature(nativeCurrency)) {
                throw new CurrencyException("Failed to verify native currency data of " + nativeCurrency.getHash());
            } else {
                CurrencyTypeData nativeCurrencyTypeData = nativeCurrency.getCurrencyTypeData();
                CurrencyTypeRegistrationData currencyTypeRegistrationData = new CurrencyTypeRegistrationData(nativeCurrency.getSymbol(), nativeCurrencyTypeData);
                if (!currencyTypeRegistrationCrypto.verifySignature(currencyTypeRegistrationData)) {
                    throw new CurrencyException("Failed to verify native currency data type of " + nativeCurrency.getCurrencyTypeData().getCurrencyType().getText());
                }
            }
            if (getNativeCurrency() == null) {
                setNativeCurrencyData(nativeCurrency);
            }
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
    public void getUpdatedCurrencyBatch(GetUpdatedCurrencyRequest getUpdatedCurrencyRequest, FluxSink<CurrencyData> fluxSink) {
        try {
            if (!getUpdatedCurrencyRequestCrypto.verifySignature(getUpdatedCurrencyRequest)) {
                log.error("Authorization check failed on request to get updated currencies.");
            }
            Map<CurrencyType, HashSet<Hash>> existingCurrencyHashesByType = getUpdatedCurrencyRequest.getCurrencyHashesByType();
            getRequiringUpdateOfCurrencyDataByType(existingCurrencyHashesByType, fluxSink);
        } finally {
            fluxSink.complete();
        }
    }

    @Override
    public CurrencyData getCurrencyFromDB(Hash currencyHash) {
        return currencies.getByHash(currencyHash);
    }

    @Override
    public void generateNativeCurrency() {
        throw new CurrencyException("Attempted to generate Native currency.");
    }

    @Override
    public void updateCurrenciesFromClusterStamp(Map<Hash, CurrencyData> clusterStampCurrenciesMap, Hash genesisAddress) {
        clusterStampCurrenciesMap.forEach((currencyHash, clusterStampCurrencyData) ->
                currencies.put(clusterStampCurrencyData)
        );
        updateCurrencyHashByTypeMapFromExistingCurrencies();
    }

    @Override
    public void handleExistingTransaction(TransactionData transactionData) {
        TransactionType transactionType = transactionData.getType();
        if (transactionType == TransactionType.TokenGeneration) {
            CurrencyData currencyData = getCurrencyData(transactionData);
            if (currencyData != null) {
                currencies.put(currencyData);
            }
        }
    }

    @Override
    public void handleMissingTransaction(TransactionData transactionData) {
        if (transactionData.getType() == TransactionType.TokenGeneration) {
            CurrencyData currencyData = getCurrencyData(transactionData);
            if (currencyData != null) {
                currencies.put(currencyData);
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

    private void getRequiringUpdateOfCurrencyDataByType(Map<CurrencyType, HashSet<Hash>> existingCurrencyHashesByType, FluxSink<CurrencyData> fluxSink) {
        currencyHashByTypeMap.forEach((localCurrencyType, localCurrencyHashes) -> {
            HashSet<Hash> existingCurrencyHashes = existingCurrencyHashesByType.get(localCurrencyType);

            localCurrencyHashes.forEach(localCurrencyHash -> {
                if (existingCurrencyHashes == null || existingCurrencyHashes.isEmpty() ||
                        !existingCurrencyHashes.contains(localCurrencyHash)) {
                    sendUpdatedCurrencyData(localCurrencyHash, fluxSink);
                }
            });
        });
    }

    private void sendUpdatedCurrencyData(Hash localCurrencyHash, FluxSink<CurrencyData> fluxSink) {
        CurrencyData updatedCurrencyData = currencies.getByHash(localCurrencyHash);
        if (updatedCurrencyData != null) {
            fluxSink.next(updatedCurrencyData);
        } else {
            throw new CurrencyException(String.format("Failed to retrieve currencyData %s", localCurrencyHash));
        }
    }

    @Override
    public void putCurrencyData(CurrencyData currencyData) {
        if (currencyData == null) {
            throw new CurrencyException("Failed to add an empty currency");
        }
        currencies.put(currencyData);
        updateCurrencyHashByTypeMap(currencyData);
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
        if (currencyData != null && currencyData.isConfirmed()) {
            throw new CurrencyException("Currency symbol is already in use.");
        }
    }

    @Override
    public boolean validateCurrencyUniquenessAndAddUnconfirmedRecord(TransactionData transactionData) {
        TokenGenerationFeeBaseTransactionData tokenGenerationFeeBaseTransactionData = getTokenGenerationFeeData(transactionData);
        OriginatorCurrencyData originatorCurrencyData = tokenGenerationFeeBaseTransactionData.getServiceData().getOriginatorCurrencyData();
        CurrencyTypeData currencyTypeData = tokenGenerationFeeBaseTransactionData.getServiceData().getCurrencyTypeData();
        Hash currencyHash = originatorCurrencyData.calculateHash();
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
        Hash currencyHash = originatorCurrencyData.calculateHash();
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
                try {
                    synchronized (addLockToLockMap(originatorHash)) {
                        UserTokenGenerationData userTokenGenerationData = userTokenGenerations.getByHash(originatorHash);
                        if (userTokenGenerationData == null) {
                            Map<Hash, Hash> transactionHashToCurrencyMap = new HashMap<>();
                            transactionHashToCurrencyMap.put(transactionData.getHash(), currencyHash);
                            userTokenGenerations.put(new UserTokenGenerationData(originatorCurrencyData.getOriginatorHash(), transactionHashToCurrencyMap));
                        } else {
                            userTokenGenerationData.getTransactionHashToCurrencyMap().put(originatorHash, currencyHash);
                            userTokenGenerations.put(userTokenGenerationData);
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
