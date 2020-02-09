package io.coti.fullnode.websocket.data;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.data.TransactionResponseData;
import io.coti.basenode.http.data.TransactionStatus;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class NotifyTransactionChange {

    private TransactionStatus status;
    private TransactionResponseData transactionData;

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
