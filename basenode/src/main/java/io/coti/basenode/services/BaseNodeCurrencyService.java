package io.coti.basenode.services;

import io.coti.basenode.crypto.CurrencyTypeRegistrationCrypto;
import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.CurrencyException;
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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class BaseNodeCurrencyService implements ICurrencyService {

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
    protected IBalanceService balanceService;
    @Autowired
    private UserCurrencyIndexes userCurrencyIndexes;
    @Autowired
    private ITransactionHelper transactionHelper;
    private Map<Hash, Hash> lockHashMap = new ConcurrentHashMap<>();
    private final Object lock = new Object();

    public void init() {
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
    public CurrencyData getCurrencyFromDB(Hash currencyHash) {
        return currencies.getByHash(currencyHash);
    }

    @Override
    public void generateNativeCurrency() {
        throw new CurrencyException("Attempted to generate Native currency.");
    }

    @Override
    public void updateCurrenciesFromClusterStamp(Map<Hash, CurrencyData> clusterStampCurrenciesMap, Hash genesisAddress) {
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
        if (transactionType == TransactionType.TokenGeneration) {
            CurrencyData currencyData = getCurrencyData(transactionData);
            if (currencyData != null) {
                currencies.put(currencyData);
            }
        }
    }

    @Override
    public void handleMissingTransaction(TransactionData transactionData) {
        boolean dspConsensus = transactionData.getDspConsensusResult().isDspConsensus();
        if (transactionData.getType() == TransactionType.TokenGeneration) {
            CurrencyData currencyData = getCurrencyData(transactionData);
            if (currencyData != null) {
                if (dspConsensus) {
                    currencyData.setConfirmed(true);
                }
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
                        UserCurrencyIndexData userCurrencyIndexData = userCurrencyIndexes.getByHash(originatorHash);
                        if (userCurrencyIndexData == null) {
                            HashSet<Hash> tokensHashSet = new HashSet<>();
                            tokensHashSet.add(currencyHash);
                            userCurrencyIndexes.put(new UserCurrencyIndexData(originatorCurrencyData.getOriginatorHash(), tokensHashSet));
                        } else {
                            userCurrencyIndexData.getTokens().add(currencyHash);
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
