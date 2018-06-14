package io.coti.cotinode.model;

import io.coti.cotinode.model.Interfaces.IEntity;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Vector;

@Data
public class Transaction implements IEntity {
    @Setter(AccessLevel.NONE) private byte[] hash;
    private Transaction leftParent;
    private Transaction rightParent;
    private List<byte[]> trustChainTransactionHashes;
    private byte[] userTrustScoreTokenHashes;
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
    private List<byte[]> baseTransactions;
    private byte[] senderNodeHash;
    private String senderNodeIpAddress;
    private byte[] userHash;
    private List<byte[]> childrenTransactions;
    private boolean isValid;

    public boolean isSource(){
        return childrenTransactions == null || childrenTransactions.size() == 0;
    }

    public boolean isConfirm(){
        return transactionConsensus && dspConsensus;
    }

    public Transaction(byte[] hash){
        this.hash = hash;
        this.trustChainTransactionHashes = new Vector<>();
        this.childrenTransactions = new Vector<>();
    }

    @Override
    public byte[] getKey() {
        return hash;
    }

    @Override
    public String toString(){
        return String.valueOf(hash);
    }

    @Override
    public boolean equals(Object other){
        if (other == this){
            return true;
        }

        if(!(other instanceof Transaction)){
            return false;
        }
        return Arrays.equals(hash, ((Transaction) other).getKey());
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
            System.out.println("Unable to attach to source, both parents are full");
            throw new RuntimeException("Unable to attach to source.");
        }
    }
}
