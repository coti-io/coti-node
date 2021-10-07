package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import java.time.Instant;

@Data
public class InvalidTransactionData implements IPropagatable, ISignable, ISignValidatable {

    private static final long serialVersionUID = -4057207227904211625L;

    private Hash hash;
    private Instant invalidationTime;
    private InvalidTransactionDataReason invalidationReason;
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

}
