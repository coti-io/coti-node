package io.coti.basenode.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.*;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.services.interfaces.ICurrencyService;
import io.coti.basenode.services.interfaces.IMintingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class BaseNodeMintingService implements IMintingService {

    @Autowired
    protected ICurrencyService currencyService;
    @Autowired
    protected Currencies currencies;

    private Map<Hash, BigDecimal> mintingMap;
    private Map<Hash, Hash> lockMintingRecordHashMap = new ConcurrentHashMap<>();

    public void init() {
        mintingMap = new ConcurrentHashMap<>();
        log.info("{} is up", this.getClass().getSimpleName());
    }

    public BigDecimal getTokenAllocatedAmount(Hash tokenHash) {
        if (mintingMap.get(tokenHash) != null) {
            return mintingMap.get(tokenHash);
        }
        return BigDecimal.ZERO;
    }

    protected Hash addLockToLockMap(Hash hash) {
        return addLockToLockMap(lockMintingRecordHashMap, hash);
    }

    private Hash addLockToLockMap(Map<Hash, Hash> locksIdentityMap, Hash hash) {
        synchronized (locksIdentityMap) {
            locksIdentityMap.putIfAbsent(hash, hash);
            return locksIdentityMap.get(hash);
        }
    }

    protected void removeLockFromLocksMap(Hash hash) {
        removeLockFromLocksMap(lockMintingRecordHashMap, hash);
    }

    private void removeLockFromLocksMap(Map<Hash, Hash> locksIdentityMap, Hash hash) {
        synchronized (locksIdentityMap) {
            Hash hashLock = locksIdentityMap.get(hash);
            if (hashLock != null) {
                locksIdentityMap.remove(hash);
            }
        }
    }

    @Override
    public boolean checkMintingAmountAndAddToAllocatedAmount(TransactionData transactionData) {
        TokenMintingFeeBaseTransactionData tokenMintingFeeBaseTransactionData = getTokenMintingFeeData(transactionData);
        Hash tokenHash = tokenMintingFeeBaseTransactionData.getServiceData().getMintingCurrencyHash();
        synchronized (addLockToLockMap(tokenHash)) {
            BigDecimal tokenAmount = tokenMintingFeeBaseTransactionData.getServiceData().getMintingAmount();

            CurrencyData currencyFromDB = currencyService.getCurrencyFromDB(tokenHash);
            if (currencyFromDB == null) {
                log.error("Error in Minting check. Token {} is invalid", tokenHash);
                return false;
            }
            if (currencyFromDB.getCurrencyTypeData().getCurrencyType().equals(CurrencyType.NATIVE_COIN)) {
                log.error("Error in Minting check. Token {} is Native currency", tokenHash);
                return false;
            }
            BigDecimal tokenAllocatedAmount = getTokenAllocatedAmount(tokenHash);
            if (tokenAllocatedAmount == null || currencyFromDB.getTotalSupply().subtract(tokenAllocatedAmount.add(tokenAmount)).signum() < 0) {
                log.error("Error in Minting check. Token {} amount {} is too much", tokenHash, tokenAmount);
                return false;
            }
            Hash receiverAddress = tokenMintingFeeBaseTransactionData.getServiceData().getReceiverAddress();
            if (!CryptoHelper.isAddressValid(receiverAddress)) {
                log.error("Error in Minting check. Token {} receiver address {} is invalid", tokenHash, receiverAddress);
                return false;
            }

            mintingMap.put(tokenHash, tokenAllocatedAmount.add(tokenAmount));
        }
        removeLockFromLocksMap(tokenHash);
        return true;
    }

    @Override
    public void revertMintingAllocation(TransactionData transactionData) {
        TokenMintingFeeBaseTransactionData tokenMintingFeeData = getTokenMintingFeeData(transactionData);
        Hash tokenHash = tokenMintingFeeData.getCurrencyHash();
        synchronized (addLockToLockMap(tokenHash)) {
            CurrencyData currencyFromDB = currencyService.getCurrencyFromDB(tokenHash);
            BigDecimal tokenAllocatedAmount = this.getTokenAllocatedAmount(tokenHash);
            if (currencyFromDB == null || tokenAllocatedAmount == null) {
                log.error("Error in Minting revert. Token {} is invalid", tokenHash);
                removeLockFromLocksMap(tokenHash);
                return;
            }
            mintingMap.put(tokenHash, tokenAllocatedAmount.subtract(tokenMintingFeeData.getAmount()));
            removeLockFromLocksMap(tokenHash);
        }
    }

    protected TokenMintingFeeBaseTransactionData getTokenMintingFeeData(TransactionData tokenMintingTransaction) {
        return (TokenMintingFeeBaseTransactionData) tokenMintingTransaction
                .getBaseTransactions()
                .stream()
                .filter(t -> t instanceof TokenMintingFeeBaseTransactionData)
                .findFirst().orElse(null);
    }

    @Override
    public void updateTokenAllocatedAmountFromDBAndClusterStamp(Hash currencyHash, BigDecimal currencyAmountInAddress) {
        CurrencyData currencyData = currencies.getByHash(currencyHash);
        if (currencyData != null) {
            mintingMap.put(currencyHash, currencyData.getTotalSupply().subtract(currencyAmountInAddress));
        }
    }

    @Override
    public void handleExistingTransaction(TransactionData transactionData) {
        TransactionType transactionType = transactionData.getType();
        if (transactionType == TransactionType.TokenMintingFee) {
            TokenMintingFeeBaseTransactionData tokenMintingFeeData = getTokenMintingFeeData(transactionData);
            if (tokenMintingFeeData != null) {
                Hash tokenHash = tokenMintingFeeData.getServiceData().getMintingCurrencyHash();
                BigDecimal newMintingRequestedAmount = tokenMintingFeeData.getServiceData().getMintingAmount();
                mintingMap.put(tokenHash, newMintingRequestedAmount.add(getTokenAllocatedAmount(tokenHash)));
            }
        }
    }

    @Override
    public void validateMintingBalances() {
        // To be validated only by Financial server
    }
}
