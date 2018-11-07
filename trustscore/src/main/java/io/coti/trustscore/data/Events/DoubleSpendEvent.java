package io.coti.trustscore.data.Events;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TransactionData;
import lombok.Data;

@Data
public class DoubleSpendEvent extends EventData {

    private TransactionData transactionData;
    private Hash transactionHash;
    private SignatureData eventSignature;

    public DoubleSpendEvent(TransactionData transactionData, SignatureData eventSignature) {
        this.setUniqueIdentifier(transactionData.getHash());
        this.transactionData = transactionData;
        this.eventSignature = eventSignature;
    }


}
