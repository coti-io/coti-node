package io.coti.basenode.http.data;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.data.interfaces.ITransactionResponseData;
import lombok.Data;

import java.time.Instant;

@Data
public class ReducedTransactionResponseData implements ITransactionResponseData {

    private String hash;
    private Instant attachmentTime;

    public ReducedTransactionResponseData(TransactionData transactionData) {
        hash = transactionData.getHash().toHexString();
        attachmentTime = transactionData.getAttachmentTime();
    }
}
