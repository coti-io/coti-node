package io.coti.basenode.services;

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
        if (!transactionHelper.validateBaseTransactionPublicKey(tokenMintingFeeBaseTransactionData, NodeType.FinancialServer)) {
            log.error("Error in Minting check. Base transaction not signed by an authorized Financial server");
            return false;
        }

        Hash tokenHash = tokenMintingFeeBaseTransactionData.getServiceData().getMintingCurrencyHash();
        try {
            synchronized (tokenHashLockData.addLockToLockMap(tokenHash)) {
                if (currencyService.isNativeCurrency(tokenHash)) {
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
        if (tokenMintingFeeData != null) {
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
    }


    @Override
    public void doTokenMinting(TransactionData transactionData) {
        TokenMintingFeeBaseTransactionData tokenMintingFeeBaseTransactionData = transactionHelper.getTokenMintingFeeData(transactionData);
        if (tokenMintingFeeBaseTransactionData != null) {
            TokenMintingServiceData tokenMintingFeeBaseTransactionServiceData = tokenMintingFeeBaseTransactionData.getServiceData();
            Hash tokenHash = tokenMintingFeeBaseTransactionServiceData.getMintingCurrencyHash();

            if (currencyService.isNativeCurrency(tokenHash)) {
                log.error("Error in Minting check. Token {} is Native currency", tokenHash);
                return;
            }
            currencyService.synchronizedUpdateMintableAmountMapAndBalance(transactionData);
            updateMintedAddress(tokenMintingFeeBaseTransactionServiceData);
        }
    }

    private void updateMintedAddress(TokenMintingServiceData tokenMintingFeeBaseTransactionServiceData) {
        Hash receiverAddressHash = tokenMintingFeeBaseTransactionServiceData.getReceiverAddress();
        Hash currencyHash = tokenMintingFeeBaseTransactionServiceData.getMintingCurrencyHash();
        balanceService.continueHandleBalanceChanges(receiverAddressHash, currencyHash);
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
