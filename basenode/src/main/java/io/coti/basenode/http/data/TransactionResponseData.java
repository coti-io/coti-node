package io.coti.basenode.http.data;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.BaseTransactionName;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.exceptions.TransactionException;
import io.coti.basenode.http.data.interfaces.ITransactionResponseData;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.TRANSACTION_RESPONSE_ERROR;

@Slf4j
@Data
public class TransactionResponseData implements ITransactionResponseData {

    private String hash;
    private BigDecimal amount;
    private TransactionType type;
    private List<BaseTransactionResponseData> baseTransactions;
    private String leftParentHash;
    private String rightParentHash;
    private boolean trustChainConsensus;
    private double trustChainTrustScore;
    private Instant transactionConsensusUpdateTime;
    private Instant createTime;
    private Instant attachmentTime;
    private double senderTrustScore;
    private List<String> childrenTransactionHashes;
    private Boolean isValid;
    private String transactionDescription;

    private TransactionResponseData() {
    }

    public TransactionResponseData(TransactionData transactionData) {

        hash = transactionData.getHash().toHexString();
        amount = transactionData.getAmount();
        type = transactionData.getType();
        baseTransactions = new ArrayList<>();
        if (transactionData.getBaseTransactions() != null) {
            transactionData.getBaseTransactions().forEach(baseTransactionData ->
            {
                try {
                    Class<? extends BaseTransactionResponseData> baseTransactionResponseDataClass = BaseTransactionResponseClass.valueOf(BaseTransactionName.getName(baseTransactionData.getClass()).name()).getBaseTransactionResponseClass();
                    Constructor<? extends BaseTransactionResponseData> constructor = baseTransactionResponseDataClass.getConstructor(BaseTransactionData.class);
                    baseTransactions.add(constructor.newInstance(baseTransactionData));
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    log.error("Transaction response error", e);
                    throw new TransactionException(TRANSACTION_RESPONSE_ERROR);
                }
            });
        }
        leftParentHash = transactionData.getLeftParentHash() == null ? null : transactionData.getLeftParentHash().toHexString();
        rightParentHash = transactionData.getRightParentHash() == null ? null : transactionData.getRightParentHash().toHexString();

        trustChainConsensus = transactionData.isTrustChainConsensus();
        trustChainTrustScore = transactionData.getTrustChainTrustScore();
        transactionConsensusUpdateTime = transactionData.getTransactionConsensusUpdateTime();
        createTime = transactionData.getCreateTime();
        attachmentTime = transactionData.getAttachmentTime();
        senderTrustScore = transactionData.getSenderTrustScore();

        childrenTransactionHashes = new ArrayList<>();
        transactionData.getChildrenTransactionHashes().forEach(childrenTransactionHash -> childrenTransactionHashes.add(childrenTransactionHash.toHexString()));
        transactionDescription = transactionData.getTransactionDescription();
        isValid = transactionData.isValid();
    }


}
