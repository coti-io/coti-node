package io.coti.common.data;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.coti.common.data.interfaces.IEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;


@Slf4j
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, defaultImpl = TransactionData.class)
public class TransactionData implements IEntity, Comparable<TransactionData> {
    private List<BaseTransactionData> baseTransactions;
    private transient Hash hash;
    private BigDecimal amount;
    private Hash leftParentHash;
    private Hash rightParentHash;
    private List<Hash> trustChainTransactionHashes;
    private Hash userTrustScoreTokenHashes;
    private boolean trustChainConsensus;
    private boolean dspConsensus;
    private double trustChainTrustScore;
    private Date transactionConsensusUpdateTime;
    private Date createTime;
    private Date attachmentTime;
    private Date processStartTime;
    private Date powStartTime;
    private Date powEndTime;
    private double senderTrustScore;
    private List<Hash> baseTransactionsHash;
    private Hash senderHash;
    private String nodeIpAddress;
    private Hash nodeHash;
    private SignatureData nodeSignature;
    private List<Hash> childrenTransactions;
    private boolean valid;
    private Map<String, Boolean> validByNodes;
    private transient boolean isVisit;
    private boolean isZeroSpend;
    private String transactionDescription;

    private TransactionData() {
    }

    public TransactionData(List<BaseTransactionData> baseTransactions, Hash transactionHash, String transactionDescription,double senderTrustScore, Date createTime) {
        this(baseTransactions,transactionDescription,senderTrustScore,createTime);
        this.hash = transactionHash;
    }

    public TransactionData(List<BaseTransactionData> baseTransactions, String transactionDescription,double senderTrustScore, Date createTime) {
        this.transactionDescription = transactionDescription;
        this.baseTransactions = baseTransactions;
        this.createTime = createTime;
        this.senderTrustScore = senderTrustScore;
        BigDecimal amount = BigDecimal.ZERO;
        for(BaseTransactionData baseTransaction : baseTransactions){
            amount = amount.add(baseTransaction.getAmount().signum() > 0 ? baseTransaction.getAmount() : BigDecimal.ZERO);
        }
        this.amount = amount;
        this.initTransactionData();
    }


    private void  initTransactionData()
    {
        this.trustChainTransactionHashes = new Vector<>();
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
    public Hash getHash() {
        return this.hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }

    public boolean hasSources() {
        return getLeftParentHash() != null || getRightParentHash() != null;
    }

    public void addNodesToTransaction(Map<String, Boolean> validByNodes) {
        this.validByNodes.putAll(validByNodes);
    }

    @Override
    public int compareTo(TransactionData other) {
        return Double.compare(this.senderTrustScore, other.senderTrustScore);
    }

    public void addSignature(String nodeId, boolean result) {
        return;
    }
}
