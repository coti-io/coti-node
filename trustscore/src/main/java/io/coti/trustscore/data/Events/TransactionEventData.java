package io.coti.trustscore.data.Events;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.http.InsertEventRequest;
import lombok.Data;

@Data
public class TransactionEventData extends EventData{

    private TransactionData transactionData;

    public TransactionEventData(InsertEventRequest request) {
        this.transactionData = request.getTransactionData();
        this.setUniqueIdentifier(transactionData.getHash());
        this.setEventDate(this.transactionData.getDspConsensusResult().getIndexingTime());
        super.setSignatureData(request.getSignature());
        super.setEventType(EventType.TRANSACTION);
    }


}
