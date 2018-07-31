package io.coti.common.services;

import io.coti.common.crypto.TransactionCryptoWrapper;
import io.coti.common.data.*;
import io.coti.common.http.AddTransactionResponse;
import io.coti.common.http.BaseResponse;
import io.coti.common.http.GetTransactionResponse;
import io.coti.common.http.data.TransactionResponseData;
import io.coti.common.model.AddressesTransactionsHistory;
import io.coti.common.model.Transactions;
import io.coti.common.services.interfaces.IBalanceService;
import io.coti.common.services.interfaces.IClusterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.coti.common.http.HttpStringConstants.STATUS_ERROR;
import static io.coti.common.http.HttpStringConstants.TRANSACTION_DOESNT_EXIST_MESSAGE;

@Slf4j
@Service
public class TransactionHelper {

    @Autowired
    private AddressesTransactionsHistory addressesTransactionsHistory;
    private Set<TransactionData> currentlyHandledTransactions = new HashSet<>();
    @Autowired
    private IBalanceService balanceService;
    @Autowired
    private IClusterService clusterService;
    @Autowired
    private Transactions transactions;

    @PostConstruct
    private void init() {
        log.info("Transaction Helper Started");
    }

    public boolean isLegalBalance(List<BaseTransactionData> baseTransactions) {
        BigDecimal totalTransactionSum = BigDecimal.ZERO;
        for (BaseTransactionData baseTransactionData :
                baseTransactions) {
            totalTransactionSum = totalTransactionSum.add(baseTransactionData.getAmount());
        }
        return totalTransactionSum.compareTo(BigDecimal.ZERO) == 0;
    }

    private void updateAddressTransactionHistory(TransactionData transactionData) {
        for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
            AddressTransactionsHistory addressHistory = addressesTransactionsHistory.getByHash(baseTransactionData.getAddressHash());

            if (addressHistory == null) {
                addressHistory = new AddressTransactionsHistory(baseTransactionData.getAddressHash());
            }
            addressHistory.addTransactionHashToHistory(transactionData.getHash());
            addressesTransactionsHistory.put(addressHistory);
        }
    }

    public boolean validateTransaction(TransactionData transactionData) {
        TransactionCryptoWrapper verifyTransaction = new TransactionCryptoWrapper(transactionData);
        return verifyTransaction.isTransactionValid();
    }

    private boolean isTransactionExists(TransactionData transactionData) {
        if (currentlyHandledTransactions.contains(transactionData)) {
            return true;
        }
        if (transactions.getByHash(transactionData.getHash()) != null) {
            return true;
        }
        return false;
    }

    public boolean startHandleTransaction(TransactionData transactionData) {
        synchronized (transactionData) {
            if(isTransactionExists(transactionData)){
                return false;
            }
            currentlyHandledTransactions.add(transactionData);
            return true;
        }
    }

    public boolean endHandleTransaction(TransactionData transactionData) {
        synchronized (transactionData) {
            currentlyHandledTransactions.remove(transactionData);
            return true;
        }
    }

    public boolean validateDataIntegrity(TransactionData transactionData) {
//        return validateAddresses(transactionData.getBaseTransactions(), transactionData.getHash(), transactionData.getTransactionDescription(), transactionData.getSenderTrustScore(), transactionData.getCreateTime());
        return true;
    }

    public ResponseEntity<BaseResponse> getTransactionDetails(Hash transactionHash) {
        TransactionData transactionData = transactions.getByHash(transactionHash);
        if (transactionData == null)
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new AddTransactionResponse(
                            STATUS_ERROR,
                            TRANSACTION_DOESNT_EXIST_MESSAGE));
        TransactionResponseData transactionResponseData = new TransactionResponseData(transactionData);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GetTransactionResponse(transactionResponseData));
    }

    public boolean checkBalancesAndAddToPreBalance(List<BaseTransactionData> baseTransactions) {
        return balanceService.checkBalancesAndAddToPreBalance(baseTransactions);
    }

    public void attachTransactionToCluster(TransactionData transactionData) {
        transactions.put(transactionData);
        updateAddressTransactionHistory(transactionData);
        balanceService.insertToUnconfirmedTransactions(new ConfirmationData(transactionData));
        clusterService.attachToCluster(transactionData);
    }
}