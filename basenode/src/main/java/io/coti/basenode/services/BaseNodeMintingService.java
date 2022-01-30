package io.coti.basenode.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.*;
import io.coti.basenode.model.Currencies;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.ICurrencyService;
import io.coti.basenode.services.interfaces.IMintingService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class BaseNodeMintingService implements IMintingService {

    @Autowired
    protected ICurrencyService currencyService;
    @Autowired
    protected Currencies currencies;
    @Autowired
    protected IBalanceService balanceService;
    @Autowired
    protected ITransactionHelper transactionHelper;
    private final LockData tokenHashLockData = new LockData();

    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public boolean checkMintingAmountAndUpdateMintableAmount(TransactionData transactionData) {
        TokenMintingFeeBaseTransactionData tokenMintingFeeBaseTransactionData = transactionHelper.getTokenMintingFeeData(transactionData);
        Hash tokenHash = tokenMintingFeeBaseTransactionData.getServiceData().getMintingCurrencyHash();
        try {
            synchronized (tokenHashLockData.addLockToLockMap(tokenHash)) {
                if (currencyService.getNativeCurrencyHash().equals(tokenHash)) {
                    log.error("Error in Minting check. Token {} is Native currency", tokenHash);
                    return false;
                }
                CurrencyData currencyData = currencies.getByHash(tokenHash);
                if (currencyData == null) {
                    log.error("Error in Minting check. Token {} is invalid", tokenHash);
                    return false;
                }
                BigDecimal mintableAmount = currencyService.getTokenMintableAmount(tokenHash);
                BigDecimal tokenAmount = tokenMintingFeeBaseTransactionData.getServiceData().getMintingAmount();
                if (mintableAmount != null) {
                    BigDecimal restAfterMinting = Optional.ofNullable(currencyService.getTokenMintableAmount(tokenHash)).orElse(BigDecimal.ZERO).subtract(tokenAmount);
                    if (restAfterMinting.signum() < 0) {
                        log.error("Error in Minting check. Token {} amount {} is too much", tokenHash, tokenAmount);
                        return false;
                    }
                } else {
                    BigDecimal expectedTotalAmount = currencyData.getTotalSupply();
                    BigDecimal postponedMintingAmount = currencyService.getPostponedMintingAmount(tokenHash);
                    if (expectedTotalAmount.subtract(postponedMintingAmount).subtract(tokenAmount).signum() < 0) {
                        log.error("Error in postponed Minting check. Token {} postponed amount {} is too much", tokenHash, postponedMintingAmount.add(tokenAmount));
                        return false;
                    }
                }
                currencyService.synchronizedUpdateMintableAmountMapAndBalance(transactionData);
            }
        } finally {
            tokenHashLockData.removeLockFromLocksMap(tokenHash);
        }
        return true;
    }

    @Override
    public void revertMintingAllocation(TransactionData transactionData) {
        TokenMintingFeeBaseTransactionData tokenMintingFeeData = transactionHelper.getTokenMintingFeeData(transactionData);
        Hash tokenHash = tokenMintingFeeData.getCurrencyHash();
        try {
            synchronized (tokenHashLockData.addLockToLockMap(tokenHash)) {
                CurrencyData currencyFromDB = currencies.getByHash(tokenHash);
                BigDecimal mintableAmount = currencyService.getTokenMintableAmount(tokenHash);
                if (currencyFromDB == null || mintableAmount == null) {
                    log.error("Error in Minting revert. Token {} is invalid", tokenHash);
                    return;
                }
                currencyService.putToMintableAmountMap(tokenHash, mintableAmount.add(tokenMintingFeeData.getAmount()));
            }
        } finally {
            tokenHashLockData.removeLockFromLocksMap(tokenHash);
        }
    }


    @Override
    public void doTokenMinting(TransactionData transactionData) {
        TokenMintingFeeBaseTransactionData tokenMintingFeeBaseTransactionData = transactionHelper.getTokenMintingFeeData(transactionData);
        TokenMintingData tokenMintingFeeBaseTransactionServiceData = tokenMintingFeeBaseTransactionData.getServiceData();
        Hash tokenHash = tokenMintingFeeBaseTransactionServiceData.getMintingCurrencyHash();

        if (currencyService.getNativeCurrencyHash().equals(tokenHash)) {
            log.error("Error in Minting check. Token {} is Native currency", tokenHash);
            return;
        }
        currencyService.synchronizedUpdateMintableAmountMapAndBalance(transactionData);
    }

    @Override
    public void handleExistingTransaction(TransactionData transactionData) {
        handleTransaction(transactionData);
    }

    @Override
    public void handleMissingTransaction(TransactionData transactionData) {
        handleTransaction(transactionData);
    }

    private void handleTransaction(TransactionData transactionData) {
        if (transactionData.getType().equals(TransactionType.TokenMinting)) {
            currencyService.synchronizedUpdateMintableAmountMapAndBalance(transactionData);
        }
    }

}
