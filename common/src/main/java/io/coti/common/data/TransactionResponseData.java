package io.coti.common.data;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.math.BigDecimal;

@Data
public class TransactionResponseData {
    private String hash;
    private BigDecimal amount;
    private String leftParentHash;
    private String rightParentHash;
    private List<String> trustChainTransactionHashes;
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
    private List<String> childrenTransactions;
    private boolean isValid;
    private boolean isZeroSpend;
    private String transactionDescription;

    public TransactionResponseData(){ }


}
