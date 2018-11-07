package io.coti.trustscore.data.Events;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TransactionData;
import lombok.Data;

@Data
public class ChargeBackEventsData extends EventData {
    private TransactionData transactionData;
    private SignatureData eventSignature;

    public ChargeBackEventsData(TransactionData transactionData, SignatureData eventSignature) {
        this.transactionData = transactionData;
        this.eventSignature = eventSignature;
    }

    @Override
    public int hashCode() {
        return transactionData.getHash().hashCode();
    }

    @Override
    public Hash getHash() {
        return this.transactionData.getHash();
    }

    @Override
    public void setHash(Hash hash) {
        this.transactionData.setHash(hash);
    }
}
