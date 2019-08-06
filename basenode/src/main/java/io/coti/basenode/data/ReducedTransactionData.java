package io.coti.basenode.data;

import lombok.Data;

import java.time.Instant;

@Data
public class ReducedTransactionData implements Comparable<ReducedTransactionData> {

    private Hash transactionHash;
    private Instant attachmentTime;

    public ReducedTransactionData(TransactionData transactionData) {
        transactionHash = transactionData.getHash();
        attachmentTime = transactionData.getAttachmentTime();
    }

    @Override
    public int compareTo(ReducedTransactionData o) {
        if (attachmentTime.equals(o.attachmentTime)) {
            return 0;
        }
        return attachmentTime.isAfter(o.attachmentTime) ? 1 : -1;
    }
}
