package io.coti.basenode.http.data;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.TransactionData;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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

        hash = transactionData.getHash().toHexString();
        amount = transactionData.getAmount();
        baseTransactionResponses = new LinkedList<>();
        if (transactionData.getBaseTransactions() != null) {
            for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()
                    ) {
                baseTransactionResponses.add(new BaseTransactionResponseData(baseTransactionData));
            }
        }
        leftParentHash = transactionData.getLeftParentHash() == null ? null : transactionData.getLeftParentHash().toHexString();
        rightParentHash = transactionData.getRightParentHash() == null ? null : transactionData.getRightParentHash().toHexString();
        trustChainTransactionHashes = new LinkedList<>();
        transactionData.getTrustChainTransactionHashes().forEach(trustChainHash -> trustChainTransactionHashes.add(trustChainHash.toHexString()));

        trustChainConsensus = transactionData.isTrustChainConsensus();
        trustChainTrustScore = transactionData.getTrustChainTrustScore();
        transactionConsensusUpdateTime = transactionData.getTransactionConsensusUpdateTime();
        createTime = transactionData.getCreateTime();
        attachmentTime = transactionData.getAttachmentTime();
        processStartTime = transactionData.getProcessStartTime();
        powStartTime = transactionData.getPowStartTime();
        powEndTime = transactionData.getPowEndTime();
        senderTrustScore = transactionData.getSenderTrustScore();

        childrenTransactions = new LinkedList<>();
        transactionData.getChildrenTransactions().forEach(childrenTransaction -> childrenTransactions.add(childrenTransaction.toHexString()));
        transactionDescription = transactionData.getTransactionDescription();
        isValid = transactionData.isValid();
        isZeroSpend = transactionData.isZeroSpend();
    }


}
