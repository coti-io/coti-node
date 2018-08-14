package io.coti.trustscore.services;

import io.coti.common.crypto.TransactionCrypto;
import io.coti.common.data.AddressTransactionsHistory;
import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.http.BaseResponse;
import io.coti.common.http.GetAddressTransactionHistory;
import io.coti.common.http.data.TransactionStatus;
import io.coti.common.model.AddressesTransactionsHistory;
import io.coti.common.model.DbItem;
import io.coti.common.model.Transactions;
import io.coti.common.services.LiveView.WebSocketSender;
import io.coti.common.services.interfaces.ITransactionHelper;
import io.coti.common.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Vector;

@Slf4j
@Service
public class TransactionService {
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private TransactionCrypto transactionCrypto;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private AddressesTransactionsHistory addressesTransactionsHistory;
    @Autowired
    private Transactions transactions;
    @Autowired
    private WebSocketSender webSocketSender;

    public ResponseEntity<BaseResponse> getAddressTransactions(Hash addressHash) {
        List<TransactionData> transactionsDataList = new Vector<>();
        DbItem<AddressTransactionsHistory> dbAddress = addressesTransactionsHistory.getByHashItem(addressHash);

        if (!dbAddress.isExists)
            return ResponseEntity.status(HttpStatus.OK).body(new GetAddressTransactionHistory(transactionsDataList));

        AddressTransactionsHistory history = dbAddress.item;
        for (Hash transactionHash : history.getTransactionsHistory()) {
            TransactionData transactionData = transactions.getByHash(transactionHash);
            transactionsDataList.add(transactionData);

        }
        return ResponseEntity.status(HttpStatus.OK).body(new GetAddressTransactionHistory(transactionsDataList));
    }

    public void handlePropagatedTransaction(TransactionData transactionData) {
        log.info("Propagated Transaction received: {}", transactionData.getHash().toHexString());
        if (!transactionHelper.startHandleTransaction(transactionData)) {
            log.info("Transaction already exists");
            return;
        }
        if (!transactionHelper.validateTransaction(transactionData) ||
                !transactionCrypto.verifySignature(transactionData) ||
                !validationService.validatePow(transactionData)) {
            log.info("Data Integrity validation failed");
            return;
        }
        if(!transactionHelper.checkBalancesAndAddToPreBalance(transactionData)){
            log.info("Balance check failed!");
            return;
        }
        transactionHelper.attachTransactionToCluster(transactionData);
        transactionHelper.setTransactionStateToSaved(transactionData);
        webSocketSender.notifyTransactionHistoryChange(transactionData, TransactionStatus.ATTACHED_TO_DAG);
        transactionHelper.setTransactionStateToFinished(transactionData);
        transactionHelper.endHandleTransaction(transactionData);
    }
}
