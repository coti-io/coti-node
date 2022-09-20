package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import java.time.Instant;

@Data
public class RejectedTransactionData implements IPropagatable, ISignable, ISignValidatable {

    private static final long serialVersionUID = -4057207227904211625L;

    private Hash hash;
    private TransactionData transactionData;
    private final Instant rejectionTime;
    private final String rejectionReasonDescription;
    private Hash nodeHash;
    private SignatureData nodeSignature;

    private RejectedTransactionData() {
        this.rejectionReasonDescription = "";
        this.rejectionTime = Instant.now();
    }

    public RejectedTransactionData(TransactionData transactionData, String rejectionReasonDescription) {
        this.hash = transactionData.getHash();
        this.transactionData = transactionData;
        this.rejectionReasonDescription = rejectionReasonDescription;
        this.rejectionTime = Instant.now();
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
