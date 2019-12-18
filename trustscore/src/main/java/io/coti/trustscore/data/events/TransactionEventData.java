package io.coti.trustscore.data.events;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.trustscore.data.enums.EventType;
import io.coti.trustscore.http.InsertEventRequest;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionEventData extends EventData {

    private static final long serialVersionUID = -88042864576760047L;
    private Hash transactionHash;
    private BigDecimal amount;

    public TransactionEventData(InsertEventRequest request) {
        TransactionData transactionData = request.getTransactionData();
        this.setTransactionHash(transactionData.getHash());
        this.setAmount(transactionData.getAmount());
        this.setUniqueIdentifier(transactionData.getHash());
        this.setEventDate(transactionData.getDspConsensusResult().getIndexingTime());
        super.setSignatureData(request.getSignature());
        super.setEventType(EventType.TRANSACTION);
    }


}
