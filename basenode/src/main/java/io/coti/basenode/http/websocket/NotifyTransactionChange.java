package io.coti.basenode.http.websocket;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.data.TransactionResponseData;
import io.coti.basenode.http.data.TransactionStatus;


public class NotifyTransactionChange {


    public TransactionStatus status;
    public TransactionResponseData transactionData;

    public NotifyTransactionChange(TransactionData transactionData, TransactionStatus transactionStatus) {
        this.status = transactionStatus;
        this.transactionData = new TransactionResponseData(transactionData);
    }
}
