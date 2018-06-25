package io.coti.cotinode.data;

import io.coti.cotinode.data.interfaces.IEntity;
import io.coti.cotinode.http.AddTransactionRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.Vector;

@Slf4j
@Data
public class TransactionData implements IEntity {
    private transient Hash hash;
    private Hash leftParentHash;
    private Hash rightParentHash;
    private List<Hash> trustChainTransactionHash;
    private Hash userTrustScoreTokenHashes;
    private boolean transactionConsensus;
    private boolean dspConsensus;
    private double trustChainTrustScore;
    private Date transactionConsensusUpdateTime;
    private Date createTime;
    private Date updateTime;
    private Date attachmentTime;
    private Date processStartTime;
    private Date processEndTime;
    private Date powStartTime;
    private Date powEndTime;
    private int baseTransactionsCount;
    private double senderTrustScore;
    private List<Hash> baseTransactionsHash;
    private List<BaseTransactionData> baseTransactions;
    private Hash senderNodeHash;
    private String senderNodeIpAddress;
    private Hash userHash;
    private List<Hash> childrenTransactions;
    private boolean isValid;
    private transient boolean isVisit;

    public void setSenderTrustScore(double value){
        senderTrustScore = value;
    }
    public int getSenderTrustScore(){
        return (int) Math.round(senderTrustScore);
    }

    private TransactionData(){
    }

    public TransactionData(AddTransactionRequest request){
        this.hash = request.transactionHash;
        this.baseTransactions = request.baseTransactions;
        this.senderTrustScore = 50;
    }

    public TransactionData(Hash hash) {
        this.hash = hash;
        this.trustChainTransactionHash = new Vector<>();
        this.childrenTransactions = new Vector<>();
        this.senderTrustScore = 50;
    }

    public boolean isSource() {
        return childrenTransactions == null || childrenTransactions.size() == 0;
    }

    public boolean isConfirm() {
        return transactionConsensus && dspConsensus;
    }

    public void addToChildrenTransactions(Hash hash) {
        childrenTransactions.add(hash);
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

    @Override
    public Hash getKey() {
        return this.hash;
    }

    @Override
    public void setKey(Hash hash) {
        this.hash = hash;
    }

    public boolean hasSources(){
        return getLeftParentHash() != null || getRightParentHash() != null;
    }
}
