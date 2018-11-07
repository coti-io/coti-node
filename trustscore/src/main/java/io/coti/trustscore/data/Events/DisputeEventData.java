package io.coti.trustscore.data.Events;


import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TransactionData;
import lombok.Data;

@Data
public class DisputeEventData extends EventData {

    private TransactionData transactionData;
    private SignatureData eventSignature;

    public DisputeEventData(TransactionData transactionData, SignatureData eventSignature) {
        this.uniqueIdentifier = transactionData.getHash();
        this.transactionData = transactionData;
        this.eventSignature = eventSignature;
    }
}
