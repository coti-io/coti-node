package io.coti.basenode.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.crypto.CurrencyRegistrarCrypto;
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
    @Autowired
    private CurrencyRegistrarCrypto currencyRegistrarCrypto;

    private Map<Hash, Hash> lockMintingRecordHashMap = new ConcurrentHashMap<>();

    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
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
    public boolean checkMintingAmountAndAddToRequestedAmount(TransactionData transactionData) {
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
            BigDecimal requestedMintedAmountSum = currencyFromDB.getRequestedMintingAmount().add(tokenAmount);
            if (currencyFromDB.getTotalSupply().subtract(currencyFromDB.getMintedAmount().add(requestedMintedAmountSum)).signum() < 0) {
                log.error("Error in Minting check. Token {} amount {} is invalid", tokenHash, tokenAmount);
                return false;
            }
            Hash receiverAddress = tokenMintingFeeBaseTransactionData.getServiceData().getReceiverAddress();
            if (!CryptoHelper.isAddressValid(receiverAddress)) {
                log.error("Error in Minting check. Token {} receiver address {} is invalid", tokenHash, receiverAddress);
                return false;
            }

            currencyFromDB.setRequestedMintingAmount(requestedMintedAmountSum);
            currencies.put(currencyFromDB);
        }
        removeLockFromLocksMap(tokenHash);
        return true;
    }

    @Override
    public void revertMintingRequestedReserve(TransactionData transactionData) {
        TokenMintingFeeBaseTransactionData tokenMintingFeeData = getTokenMintingFeeData(transactionData);
        Hash tokenHash = tokenMintingFeeData.getCurrencyHash();
        synchronized (addLockToLockMap(tokenHash)) {
            CurrencyData currencyFromDB = currencyService.getCurrencyFromDB(tokenHash);
            if (currencyFromDB == null) {
                log.error("Error in Minting revert. Token {} is invalid", tokenHash);
                removeLockFromLocksMap(tokenHash);
                return;
            }
            currencyFromDB.setRequestedMintingAmount(currencyFromDB.getRequestedMintingAmount().subtract(tokenMintingFeeData.getAmount()));
            currencies.put(currencyFromDB);
            removeLockFromLocksMap(tokenHash);
        }
    }

    @Override
    public void updateMintedAmount(TransactionData transactionData) {
        TokenMintingFeeBaseTransactionData tokenMintingFeeBaseTransactionData = getTokenMintingFeeData(transactionData);
        Hash tokenHash = tokenMintingFeeBaseTransactionData.getServiceData().getMintingCurrencyHash();
        BigDecimal newAmountToMint = tokenMintingFeeBaseTransactionData.getServiceData().getMintingAmount();
        synchronized (addLockToLockMap(tokenHash)) {
            CurrencyData currencyFromDB = currencies.getByHash(tokenHash);
            if (currencyFromDB != null) {
                currencyFromDB.setMintedAmount(currencyFromDB.getMintedAmount().add(newAmountToMint));
                currencyFromDB.setRequestedMintingAmount(currencyFromDB.getRequestedMintingAmount().subtract(newAmountToMint));
                currencies.put(currencyFromDB);
            }
        }
    }

    protected TokenMintingFeeBaseTransactionData getTokenMintingFeeData(TransactionData tokenMintingTransaction) {
        return (TokenMintingFeeBaseTransactionData) tokenMintingTransaction
                .getBaseTransactions()
                .stream()
                .filter(t -> t instanceof TokenMintingFeeBaseTransactionData)
                .findFirst().orElse(null);
    }

}
