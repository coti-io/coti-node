package io.coti.basenode.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.*;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.services.interfaces.IBalanceService;
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
    protected IBalanceService balanceService;
    private final Object lock = new Object();
    private final Map<Hash, Hash> lockMintingRecordHashMap = new ConcurrentHashMap<>();

    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    private Hash addLockToLockMap(Hash hash) {
        synchronized (lock) {
            lockMintingRecordHashMap.putIfAbsent(hash, hash);
            return lockMintingRecordHashMap.get(hash);
        }
    }

    private void removeLockFromLocksMap(Hash hash) {
        synchronized (lock) {
            lockMintingRecordHashMap.remove(hash);
        }
    }

    @Override
    public boolean checkMintingAmountAndAddToAllocatedAmount(TransactionData transactionData) {
        TokenMintingFeeBaseTransactionData tokenMintingFeeBaseTransactionData = getTokenMintingFeeData(transactionData);
        Hash tokenHash = tokenMintingFeeBaseTransactionData.getServiceData().getMintingCurrencyHash();
        try {
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
                BigDecimal tokenAllocatedAmount = currencyService.getTokenAllocatedAmount(tokenHash);
                if (tokenAllocatedAmount == null || currencyFromDB.getTotalSupply().subtract(tokenAllocatedAmount.add(tokenAmount)).signum() < 0) {
                    log.error("Error in Minting check. Token {} amount {} is too much", tokenHash, tokenAmount);
                    return false;
                }
                Hash receiverAddress = tokenMintingFeeBaseTransactionData.getServiceData().getReceiverAddress();
                if (!CryptoHelper.isAddressValid(receiverAddress)) {
                    log.error("Error in Minting check. Token {} receiver address {} is invalid", tokenHash, receiverAddress);
                    return false;
                }

                currencyService.putToMintingMap(tokenHash, tokenAllocatedAmount.add(tokenAmount));
            }
        } finally {
            removeLockFromLocksMap(tokenHash);
        }
        return true;
    }

    @Override
    public void revertMintingAllocation(TransactionData transactionData) {
        TokenMintingFeeBaseTransactionData tokenMintingFeeData = getTokenMintingFeeData(transactionData);
        Hash tokenHash = tokenMintingFeeData.getCurrencyHash();
        try {
            synchronized (addLockToLockMap(tokenHash)) {
                CurrencyData currencyFromDB = currencyService.getCurrencyFromDB(tokenHash);
                BigDecimal tokenAllocatedAmount = currencyService.getTokenAllocatedAmount(tokenHash);
                if (currencyFromDB == null || tokenAllocatedAmount == null) {
                    log.error("Error in Minting revert. Token {} is invalid", tokenHash);
                    return;
                }
                currencyService.putToMintingMap(tokenHash, tokenAllocatedAmount.subtract(tokenMintingFeeData.getAmount()));
            }
        } finally {
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
    public void handleExistingTransaction(TransactionData transactionData) {
        TransactionType transactionType = transactionData.getType();
        if (transactionType == TransactionType.TokenMinting) {
            updateMintingMap(transactionData);
        }
    }

    private void updateMintingMap(TransactionData transactionData) {
        TokenMintingFeeBaseTransactionData tokenMintingFeeData = getTokenMintingFeeData(transactionData);
        if (tokenMintingFeeData != null) {
            Hash tokenHash = tokenMintingFeeData.getServiceData().getMintingCurrencyHash();
            BigDecimal newMintingRequestedAmount = tokenMintingFeeData.getServiceData().getMintingAmount();
            BigDecimal tokenAllocatedAmount = currencyService.getTokenAllocatedAmount(tokenHash);
            if (currencyService.getTokenAllocatedAmount(tokenHash) != null) {
                currencyService.putToMintingMap(tokenHash, newMintingRequestedAmount.add(tokenAllocatedAmount));
            }
        }
    }

    @Override
    public void handleMissingTransaction(TransactionData transactionData) {
        if (transactionData.getType() == TransactionType.TokenMinting) {
            updateMintingMap(transactionData);
        }
    }

    @Override
    public void validateMintingBalances() {
        // To be validated only by Financial server
    }

    @Override
    public void updateMintingBalanceFromClusterStamp(Map<Hash, ClusterStampCurrencyData> clusterStampCurrencyMap, Hash currencyGenesisAddress) {
        clusterStampCurrencyMap.forEach((currencyHash, clusterStampCurrencyData) -> {
            BigDecimal totalSupply = clusterStampCurrencyData.getTotalSupply();
            BigDecimal genesisAddressBalance = balanceService.getBalance(currencyGenesisAddress, currencyHash);
            currencyService.putToMintingMap(currencyHash, totalSupply.subtract(genesisAddressBalance));
        });
    }
}
