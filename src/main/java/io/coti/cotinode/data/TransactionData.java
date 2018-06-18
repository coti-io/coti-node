package io.coti.cotinode.data;

import io.coti.cotinode.data.interfaces.IEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.Vector;

@Slf4j
@Data
public class TransactionData implements IEntity {
    private transient Hash hash;
    private TransactionData leftParent;
    private TransactionData rightParent;
    private List<Hash> trustChainTransactionHashes;
    private Hash userTrustScoreTokenHashes;
    private boolean transactionConsensus;
    private boolean dspConsensus;
    private int totalTrustScore;
    private Date createTime;
    private Date updateTime;
    private Date attachmentTime;
    private Date processStartTime;
    private Date processEndTime;
    private Date powStartTime;
    private Date powEndTime;
    private int baseTransactionsCount;
    private int senderTrustScore;
    private List<Hash> baseTransactions;
    private Hash senderNodeHash;
    private String senderNodeIpAddress;
    private Hash userHash;
    private List<Hash> childrenTransactions;
    private boolean isValid;
    private transient boolean isVisit;

    public TransactionData(Hash hash) {
        this.hash = hash;
        this.trustChainTransactionHashes = new Vector<>();
        this.childrenTransactions = new Vector<>();
    }

    public boolean isSource() {
        return childrenTransactions == null || childrenTransactions.size() == 0;
    }

    public boolean isConfirm() {
        return transactionConsensus && dspConsensus;
    }

    @Override
    public String toString() {
        return String.valueOf(hash);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof TransactionData)) {
            return false;
        }
        return hash.equals(((TransactionData) other).hash);
    }

    public void attachToSource(TransactionData source) {
        if (leftParent == null) {
            leftParent = source;
            source.childrenTransactions.add(hash);
        } else if (rightParent == null) {
            rightParent = source;
            source.childrenTransactions.add(hash);
        } else {
            log.error("Unable to attach to source, both parents are full");
            throw new RuntimeException("Unable to attach to source.");
        }
    }

    @Override
    public Hash getKey() {
        return this.hash;
    }

    @Override
    public void setKey(Hash hash) {
        this.hash = hash;
    }
}
