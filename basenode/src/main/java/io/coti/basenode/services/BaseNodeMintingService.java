package io.coti.basenode.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.CurrencyException;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.ICurrencyService;
import io.coti.basenode.services.interfaces.IMintingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.MutablePair;
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
    private IBalanceService balanceService;
    @Autowired
    protected Currencies currencies;
    @Autowired
    private Transactions transactions;

    private Map<Hash, MutablePair> mintingMap;
    private Map<Hash, Hash> lockMintingRecordHashMap = new ConcurrentHashMap<>();

    public void init() {
        mintingMap = new ConcurrentHashMap<>();
        initMintingMap();
        log.info("{} is up", this.getClass().getSimpleName());
    }

    private void initMintingMap() {
        initMintingMapWithMinted();
        initMintingMapWithRequestedMinted();
    }

    private void initMintingMapWithMinted() {

        Map<Hash, BigDecimal> totalBalancesByCurrency = balanceService.getTotalBalancesByCurrency();
        totalBalancesByCurrency.forEach((currencyHash, currencyBalanceTotal) -> {
            mintingMap.putIfAbsent(currencyHash, new MutablePair(currencyBalanceTotal, BigDecimal.ZERO));
        });
    }

    private void initMintingMapWithRequestedMinted() {
        transactions.forEach(transactionData -> {
            TokenMintingFeeBaseTransactionData tokenMintingFeeData = getTokenMintingFeeData(transactionData);
            if (tokenMintingFeeData != null) {
                Hash tokenHash = tokenMintingFeeData.getCurrencyHash();
                BigDecimal newMintingRequestedAmount = tokenMintingFeeData.getServiceData().getMintingAmount();
                MutablePair mintingPair = mintingMap.get(tokenHash);
                if (mintingPair == null) {
                    throw new CurrencyException("Error at minting service init. Transaction " + transactionData.getHash() + " with illegal token " + tokenHash + " \n");
                }
                mintingMap.get(tokenHash).setRight(newMintingRequestedAmount.add(getTokenRequestedMintingAmount(tokenHash)));
            }
        });
    }

    public BigDecimal getTokenMintedAmount(Hash tokenHash) {
        if (mintingMap.get(tokenHash) != null) {
            return (BigDecimal) mintingMap.get(tokenHash).getLeft();
        }
        log.error("Error in getting minting amount for Token {} which is invalid", tokenHash);
        return null;
    }

    private void setTokenMintedAmount(Hash tokenHash, BigDecimal mintedAmount) {
        if (mintingMap.get(tokenHash) != null) {
            mintingMap.get(tokenHash).setLeft(mintedAmount);
        } else {
            log.error("Error in setting minting amount for Token {} which is invalid", tokenHash);
        }
    }

    public BigDecimal getTokenRequestedMintingAmount(Hash tokenHash) {
        if (mintingMap.get(tokenHash) != null) {
            return (BigDecimal) mintingMap.get(tokenHash).getRight();
        }
        log.error("Error in getting requested minting amount for Token {} which is invalid", tokenHash);
        return null;
    }

    private void setTokenRequestedMintingAmount(Hash tokenHash, BigDecimal mintedRequestedAmount) {
        if (mintingMap.get(tokenHash) != null) {
            mintingMap.get(tokenHash).setRight(mintedRequestedAmount);
        } else {
            log.error("Error in setting requested minting amount for Token {} which is invalid", tokenHash);
        }
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
            BigDecimal tokenRequestedMintingAmount = getTokenRequestedMintingAmount(tokenHash);
            BigDecimal tokenMintedAmount = getTokenMintedAmount(tokenHash);
            if (tokenRequestedMintingAmount == null || tokenMintedAmount == null
                    || currencyFromDB.getTotalSupply().subtract(tokenMintedAmount.add(tokenRequestedMintingAmount.add(tokenAmount))).signum() < 0) {
                log.error("Error in Minting check. Token {} amount {} is invalid", tokenHash, tokenAmount);
                return false;
            }
            Hash receiverAddress = tokenMintingFeeBaseTransactionData.getServiceData().getReceiverAddress();
            if (!CryptoHelper.isAddressValid(receiverAddress)) {
                log.error("Error in Minting check. Token {} receiver address {} is invalid", tokenHash, receiverAddress);
                return false;
            }

            setTokenRequestedMintingAmount(tokenHash, tokenRequestedMintingAmount.add(tokenAmount));
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
            BigDecimal tokenRequestedMintingAmount = getTokenRequestedMintingAmount(tokenHash);
            if (currencyFromDB == null || tokenRequestedMintingAmount == null) {
                log.error("Error in Minting revert. Token {} is invalid", tokenHash);
                removeLockFromLocksMap(tokenHash);
                return;
            }
            setTokenRequestedMintingAmount(tokenHash, tokenRequestedMintingAmount.subtract(tokenMintingFeeData.getAmount()));
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
            BigDecimal tokenMintedAmount = getTokenMintedAmount(tokenHash);
            BigDecimal tokenRequestedMintingAmount = getTokenRequestedMintingAmount(tokenHash);
            if (currencyFromDB != null && tokenMintedAmount != null && tokenRequestedMintingAmount != null) {
                setTokenMintedAmount(tokenHash, tokenMintedAmount.add(newAmountToMint));
                setTokenRequestedMintingAmount(tokenHash, tokenRequestedMintingAmount.subtract(newAmountToMint));
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
