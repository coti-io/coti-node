package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class TransactionData implements IPropagatable, Comparable<TransactionData>, ISignable, ISignValidatable {

    private static final long serialVersionUID = 6409430318206872225L;
    private List<BaseTransactionData> baseTransactions;
    private Hash hash;
    private BigDecimal amount;
    private TransactionType type;
    private Hash leftParentHash;
    private Hash rightParentHash;
    private boolean trustChainConsensus;
    private double trustChainTrustScore;
    private Instant trustChainConsensusTime;
    private Instant transactionConsensusUpdateTime;
    private Instant createTime;
    private Instant attachmentTime;
    private double senderTrustScore;
    private Hash senderHash;
    private SignatureData senderSignature;
    private Hash nodeHash;
    private SignatureData nodeSignature;
    private List<Hash> childrenTransactionHashes;
    private Boolean valid;
    private transient boolean isVisit;
    private String transactionDescription;
    private DspConsensusResult dspConsensusResult;
    private List<TransactionTrustScoreData> trustScoreResults;
    private int[] nonces;

    private TransactionData() {
    }

    public TransactionData(List<BaseTransactionData> baseTransactions) {
        this.baseTransactions = baseTransactions;
    }

    public TransactionData(List<BaseTransactionData> baseTransactions, Hash transactionHash, String transactionDescription, double senderTrustScore, Instant createTime, TransactionType type) {
        this(baseTransactions, transactionDescription, senderTrustScore, createTime, type);
        this.hash = transactionHash;
    }

    public TransactionData(List<BaseTransactionData> baseTransactions, String transactionDescription, double senderTrustScore, Instant createTime, TransactionType type) {
        this.transactionDescription = transactionDescription;
        this.baseTransactions = baseTransactions;
        this.createTime = createTime;
        this.type = type;
        this.senderTrustScore = senderTrustScore;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (BaseTransactionData baseTransaction : baseTransactions) {
            totalAmount = totalAmount.add(baseTransaction.getAmount().signum() > 0 ? baseTransaction.getAmount() : BigDecimal.ZERO);
        }
        this.amount = totalAmount;
        this.initTransactionData();
    }

    public TransactionData(List<BaseTransactionData> baseTransactions, Hash transactionHash, String transactionDescription, List<TransactionTrustScoreData> trustScoreResults, Instant createTime, Hash senderHash, SignatureData senderSignature, TransactionType type) {
        this.hash = transactionHash;
        this.transactionDescription = transactionDescription;
        this.baseTransactions = baseTransactions;
        this.createTime = createTime;
        this.type = type;
        this.senderHash = senderHash;
        this.senderSignature = senderSignature;
        this.trustScoreResults = trustScoreResults;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (BaseTransactionData baseTransaction : baseTransactions) {
            totalAmount = totalAmount.add(baseTransaction.getAmount().signum() > 0 ? baseTransaction.getAmount() : BigDecimal.ZERO);
        }
        this.amount = totalAmount;
        this.initTransactionData();
    }

    public TransactionData(List<BaseTransactionData> baseTransactions, Hash transactionHash, String transactionDescription, List<TransactionTrustScoreData> trustScoreResults, Instant createTime, Hash senderHash, TransactionType type) {
        this(baseTransactions, transactionHash, transactionDescription, trustScoreResults, createTime, senderHash, null, type);
    }

    private void initTransactionData() {
        this.childrenTransactionHashes = new ArrayList<>();
    }

    @JsonIgnore
    public int getRoundedSenderTrustScore() {
        return (int) Math.round(senderTrustScore);
    }

    @JsonIgnore
    public boolean isSource() {
        return childrenTransactionHashes == null || childrenTransactionHashes.isEmpty();
    }

    @JsonIgnore
    public boolean isGenesis() {
        return leftParentHash == null && rightParentHash == null;
    }

    public void addToChildrenTransactions(Hash hash) {
        childrenTransactionHashes.add(hash);
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
    public int hashCode() {
        return Arrays.hashCode(hash.getBytes());
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

    public Boolean isValid() {
        return valid;
    }

    @JsonIgnore
    public List<OutputBaseTransactionData> getOutputBaseTransactions() {
        return this.getBaseTransactions().stream().filter(baseTransactionData -> baseTransactionData instanceof OutputBaseTransactionData).map(OutputBaseTransactionData.class::cast).collect(Collectors.toList());
    }

    @JsonIgnore
    public List<InputBaseTransactionData> getInputBaseTransactions() {
        return this.getBaseTransactions().stream().filter(baseTransactionData -> baseTransactionData instanceof InputBaseTransactionData).map(InputBaseTransactionData.class::cast).collect(Collectors.toList());
    }

    @Override
    public int compareTo(TransactionData other) {
        return Double.compare(this.senderTrustScore, other.senderTrustScore);
    }

    @Override
    @JsonIgnore
    public SignatureData getSignature() {
        return nodeSignature;
    }

    @Override
    public void setSignature(SignatureData signature) {
        nodeSignature = signature;
    }

    @Override
    @JsonIgnore
    public Hash getSignerHash() {
        return nodeHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        nodeHash = signerHash;
    }

    @Override
    public String toString() {
        return this.hash.toString();
    }
}
