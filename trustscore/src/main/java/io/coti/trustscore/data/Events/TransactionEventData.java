package io.coti.trustscore.data.Events;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.http.InsertEventRequest;
import lombok.Data;

import java.nio.ByteBuffer;

@Data
public class TransactionEventData extends EventData {

    private TransactionData transactionData;

    public TransactionEventData(InsertEventRequest request) {
        this.transactionData = request.getTransactionData();

        this.setUniqueIdentifier(new Hash(ByteBuffer.allocate(transactionData.getHash().getBytes().length)
                .put(transactionData.getHash().getBytes())
                .array()));
        this.setEventDate(this.transactionData.getDspConsensusResult().getIndexingTime());
        super.setSignatureData(request.getSignature());
        super.setEventType(EventType.TRANSACTION);
    }
}
