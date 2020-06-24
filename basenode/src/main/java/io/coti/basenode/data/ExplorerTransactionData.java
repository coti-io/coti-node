package io.coti.basenode.data;

import lombok.Data;

import java.time.Instant;

@Data
public class ExplorerTransactionData implements Comparable<ExplorerTransactionData> {

    private Hash transactionHash;
    private Instant attachmentTime;

    public ExplorerTransactionData(TransactionData transactionData) {
        transactionHash = transactionData.getHash();
        attachmentTime = transactionData.getAttachmentTime();
    }

    @Override
    public int compareTo(ExplorerTransactionData o) {
        if (attachmentTime.equals(o.attachmentTime)) {
            return 0;
        }
        return attachmentTime.isAfter(o.attachmentTime) ? 1 : -1;
    }
}
