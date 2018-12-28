package io.coti.basenode.http.websocket;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.data.TransactionResponseData;
import io.coti.basenode.http.data.TransactionStatus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NotifyTransactionChange {


    public TransactionStatus status;
    public TransactionResponseData transactionData;

    public NotifyTransactionChange(TransactionData transactionData, TransactionStatus transactionStatus) {
        this.status = transactionStatus;
        try {
            this.transactionData = new TransactionResponseData(transactionData);
        } catch (Exception e) {
            log.error("NotifyTransactionChange for transaction {} failed", transactionData.getHash());
            log.error(e.getMessage());
        }
    }
}
