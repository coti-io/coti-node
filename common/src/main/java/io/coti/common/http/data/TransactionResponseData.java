package io.coti.common.http.data;

import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Vector;

@Data
public class TransactionResponseData {
    private String hash;
    private BigDecimal amount;
    private List<BaseTransactionResponseData> baseTransactionResponses;
    private String leftParentHash;
    private String rightParentHash;
    private List<String> trustChainTransactionHashes;
    private boolean trustChainConsensus;
    private double trustChainTrustScore;
    private Date transactionConsensusUpdateTime;
    private Date createTime;
    private Date attachmentTime;
    private Date processStartTime;
    private Date powStartTime;
    private Date powEndTime;
    private double senderTrustScore;
    private List<String> childrenTransactions;
    private Boolean isValid;
    private boolean isZeroSpend;
    private String transactionDescription;


    public TransactionResponseData() {
    }

    public TransactionResponseData(TransactionData transactionData) {

        this.hash = transactionData.getHash().toHexString();
        this.amount = transactionData.getAmount();
        this.baseTransactionResponses = new Vector<>();
        if (transactionData.getBaseTransactions() != null) {
            for (BaseTransactionData bxData : transactionData.getBaseTransactions()
            ) {
                this.baseTransactionResponses.add(new BaseTransactionResponseData(bxData));
            }
        }
        this.leftParentHash = transactionData.getLeftParentHash() == null ? null : transactionData.getLeftParentHash().toHexString();
        this.rightParentHash = transactionData.getRightParentHash() == null ? null : transactionData.getRightParentHash().toHexString();
        List<String> trustChainTransactionHashes = new Vector<>();
        for (Hash trustChainHash : transactionData.getTrustChainTransactionHashes()) {
            trustChainTransactionHashes.add(trustChainHash.toHexString());
        }
        this.trustChainTransactionHashes = trustChainTransactionHashes;
        this.trustChainConsensus = transactionData.isTrustChainConsensus();
        this.trustChainTrustScore = transactionData.getTrustChainTrustScore();
        this.transactionConsensusUpdateTime = transactionData.getTransactionConsensusUpdateTime();
        this.createTime = transactionData.getCreateTime();
        this.attachmentTime = transactionData.getAttachmentTime();
        this.processStartTime = transactionData.getProcessStartTime();
        this.powStartTime = transactionData.getPowStartTime();
        this.powEndTime = transactionData.getPowEndTime();
        this.senderTrustScore = transactionData.getSenderTrustScore();

        List<String> childrenTransactions = new Vector<>();
        for (Hash childrenTransaction : transactionData.getChildrenTransactions()) {
            childrenTransactions.add(childrenTransaction.toHexString());
        }
        this.childrenTransactions = childrenTransactions;
        this.transactionDescription = transactionData.getTransactionDescription();
        this.isValid = transactionData.isValid();
        this.isZeroSpend = transactionData.isZeroSpend();
    }
}
