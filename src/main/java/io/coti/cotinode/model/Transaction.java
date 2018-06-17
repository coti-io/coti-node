package io.coti.cotinode.model;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.IEntity;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;

@Slf4j
@Data
public class Transaction implements IEntity {
    @Setter(AccessLevel.NONE) private Hash hash;
    private Transaction leftParent;
    private Transaction rightParent;
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

    public boolean isSource(){
        return childrenTransactions == null || childrenTransactions.size() == 0;
    }

    public boolean isConfirm(){
        return transactionConsensus && dspConsensus;
    }

    public Transaction(Hash hash){
        this.hash = hash;
        this.trustChainTransactionHashes = new Vector<>();
        this.childrenTransactions = new Vector<>();
    }

    @Override
    public Hash getKey() {
        return hash;
    }

    @Override
    public String toString(){
        return String.valueOf(hash);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Transaction)) {
            return false;
        }
        return hash.equals(((Transaction) other).hash);
    }

    public void attachToSource(Transaction source){
        if (leftParent == null){
            leftParent = source;
            source.childrenTransactions.add(hash);
        }
        else if(rightParent == null){
            rightParent = source;
            source.childrenTransactions.add(hash);
        }
        else{
            log.error("Unable to attach to source, both parents are full");
            throw new RuntimeException("Unable to attach to source.");
        }
    }
}
