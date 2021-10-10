package io.coti.basenode.data;

import lombok.Data;

import java.time.Instant;

@Data
public class ExplorerTransactionData implements Comparable<ExplorerTransactionData> {

    private Hash transactionHash;
    private Instant attachmentTime;
    private boolean isRevert;

    public ExplorerTransactionData(TransactionData transactionData) {
        transactionHash = transactionData.getHash();
        attachmentTime = transactionData.getAttachmentTime();
        isRevert = false;
    }

    @Override
    public int compareTo(ExplorerTransactionData o) {
        if (attachmentTime.equals(o.attachmentTime)) {
            return transactionHash.compareTo(o.transactionHash);
        }
        return attachmentTime.isAfter(o.attachmentTime) ? 1 : -1;
    }
}
