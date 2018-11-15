package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Data
public class TransactionData implements IEntity, Comparable<TransactionData>, ISignable, ISignValidatable {
    private List<BaseTransactionData> baseTransactions;
    private Hash hash;
    private BigDecimal amount;
    private TransactionType type;
    private Hash leftParentHash;
    private Hash rightParentHash;
    private List<Hash> trustChainTransactionHashes;
    private Hash userTrustScoreTokenHashes;
    private boolean trustChainConsensus;
    private double trustChainTrustScore;
    private Date transactionConsensusUpdateTime;
    private Date createTime;
    private Date attachmentTime;
    private Date processStartTime;
    private Date powStartTime;
    private Date powEndTime;
    private double senderTrustScore;
    private Hash senderHash;
    private SignatureData senderSignature;
    private Hash nodeHash;
    private SignatureData nodeSignature;
    private List<Hash> childrenTransactions;
    private Boolean valid;
    private Map<String, Boolean> validByNodes;
    private transient boolean isVisit;
    private boolean isZeroSpend;
    private boolean isGenesis;
    private String transactionDescription;
    private DspConsensusResult dspConsensusResult;
    private List<TransactionTrustScoreData> trustScoreResults;
    private int[] nonces;

    private TransactionData() {
    }

    public TransactionData(List<BaseTransactionData> baseTransactions) {
        this.baseTransactions = baseTransactions;
    }

    public TransactionData(List<BaseTransactionData> baseTransactions, Hash transactionHash, String transactionDescription, double senderTrustScore, Date createTime) {
        this(baseTransactions, transactionDescription, senderTrustScore, createTime);
        this.hash = transactionHash;
    }

    public TransactionData(List<BaseTransactionData> baseTransactions, String transactionDescription, double senderTrustScore, Date createTime) {
        this.transactionDescription = transactionDescription;
        this.baseTransactions = baseTransactions;
        this.createTime = createTime;
        this.senderTrustScore = senderTrustScore;
        BigDecimal amount = BigDecimal.ZERO;
        for (BaseTransactionData baseTransaction : baseTransactions) {
            amount = amount.add(baseTransaction.getAmount().signum() > 0 ? baseTransaction.getAmount() : BigDecimal.ZERO);
        }
        this.amount = amount;
        this.initTransactionData();
    }

    public TransactionData(List<BaseTransactionData> baseTransactions, Hash transactionHash, String transactionDescription, List<TransactionTrustScoreData> trustScoreResults, Date createTime, Hash senderHash) {
        this.hash = transactionHash;
        this.transactionDescription = transactionDescription;
        this.baseTransactions = baseTransactions;
        this.createTime = createTime;
        this.senderHash = senderHash;
        this.trustScoreResults = trustScoreResults;
        BigDecimal amount = BigDecimal.ZERO;
        for (BaseTransactionData baseTransaction : baseTransactions) {
            amount = amount.add(baseTransaction.getAmount().signum() > 0 ? baseTransaction.getAmount() : BigDecimal.ZERO);
        }
        this.amount = amount;
        this.initTransactionData();
    }

    private void initTransactionData() {
        this.trustChainTransactionHashes = new Vector<>();
        this.childrenTransactions = new LinkedList<>();
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

    public Boolean isValid() {
        return valid;
    }

    public List<BaseTransactionData> getOutputBaseTransactions() {
        return this.getBaseTransactions().stream().filter(baseTransactionData -> baseTransactionData.getAmount().signum() > 0).collect(Collectors.toList());
    }

    public List<BaseTransactionData> getInputBaseTransactions() {
        return this.getBaseTransactions().stream().filter(baseTransactionData -> baseTransactionData.getAmount().signum() <= 0).collect(Collectors.toList());
    }

    @Override
    public int compareTo(TransactionData other) {
        return Double.compare(this.senderTrustScore, other.senderTrustScore);
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        nodeHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        nodeSignature = signature;
    }

    @Override
    public SignatureData getSignature() {
        return nodeSignature;
    }

    @Override
    public Hash getSignerHash() {
        return nodeHash;
    }

    @Override
    public String toString() {
        return this.hash.toString();
    }
}
