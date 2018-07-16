package io.coti.common.data;


import io.coti.common.data.interfaces.IEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;


@Slf4j
@Data
public class TransactionData implements IEntity {
    private List<BaseTransactionData> baseTransactions;
    private transient Hash hash;

    private Hash leftParentHash;
    private Hash rightParentHash;
    private List<Hash> trustChainTransactionHash;
    private Hash userTrustScoreTokenHashes;
    private boolean trustChainConsensus;
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
    private Hash senderHash;
    private String nodeIpAddress;
    private Hash nodeHash;
    private List<Hash> childrenTransactions;
    private boolean isValid;
    private Map<String, Boolean> validByNodes;
    private transient boolean isVisit;
    private boolean isZeroSpend;
    private String transactionDescription;

    private long index;


    private TransactionData() {
    }



    public TransactionData(List<BaseTransactionData> baseTransactions, Hash transactionHash, String transactionDescription,double senderTrustScore) {
        this.hash = transactionHash;
        this.transactionDescription = transactionDescription;
        this.baseTransactions = baseTransactions;
        this.senderTrustScore = senderTrustScore;

        this.initTransactionData();
    }

    private void  initTransactionData()
    {
        this.trustChainTransactionHash = new Vector<>();
        this.childrenTransactions = new Vector<>();
        this.processStartTime = (new Date());
    }





    public int getRoundedSenderTrustScore() {
        return (int) Math.round(senderTrustScore);
    }

    public boolean isSource() {
        return childrenTransactions == null || childrenTransactions.size() == 0;
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

    public boolean hasSources() {
        return getLeftParentHash() != null || getRightParentHash() != null;
    }

    public void addNodesToTransaction(Map<String, Boolean> validByNodes) {
        this.validByNodes.putAll(validByNodes);
    }

}
