package io.coti.trustscore.data.Events;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.http.InsertEventRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class TransactionEventData extends EventData {

    private static final long serialVersionUID = -88042864576760047L;
    private Hash transactionHash;
    private BigDecimal amount;

    public TransactionEventData(InsertEventRequest request) {
        TransactionData transactionData = request.getTransactionData();
        this.transactionHash = transactionData.getHash();
        this.amount = transactionData.getAmount();
        this.setUniqueIdentifier(transactionData.getHash());
        this.setEventDate(transactionData.getDspConsensusResult().getIndexingTime());
        this.setEventSignature(request.getSignature());
        this.setEventType(EventType.TRANSACTION);
    }


}
