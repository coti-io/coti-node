package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import java.time.Instant;

@Data
public class InvalidTransactionData implements IPropagatable, Comparable<TransactionData>, ISignable, ISignValidatable {

    private Hash hash;
    private Instant invalidationTime;
    private String invalidationReason;
    private Hash nodeHash;
    private SignatureData nodeSignature;

    private InvalidTransactionData() {
    }

    public InvalidTransactionData(TransactionData transactionData) {
        this.hash = transactionData.getHash();
        this.invalidationTime = Instant.now();
    }

    @Override
    @JsonIgnore
    public SignatureData getSignature() {
        return nodeSignature;
    }

    @Override
    public void setSignature(SignatureData signature) {
        nodeSignature = signature;
    }

    @Override
    @JsonIgnore
    public Hash getSignerHash() {
        return nodeHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        nodeHash = signerHash;
    }

    @Override
    public int compareTo(TransactionData o) {
        return 0;
    }
}
