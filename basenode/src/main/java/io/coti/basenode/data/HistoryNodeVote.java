package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

@Data
public class HistoryNodeVote implements IPropagatable, ISignable, ISignValidatable
{
    public Hash voterHistoryNodeHash;
    public Hash requestHash;    // TODO verify if this is same as
    public boolean isValidRequest;
    public SignatureData signature;

    private HistoryNodeVote () { }

    public HistoryNodeVote(Hash requestHash, boolean isValidRequest)
    {
        this.requestHash = requestHash;
        this.isValidRequest = isValidRequest;
    }

    @Override
    public Hash getHash() {
        return this.requestHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.requestHash = hash;

    }

    @Override
    public SignatureData getSignature() {
        return signature;
    }

    @Override
    public Hash getSignerHash() {
        return voterHistoryNodeHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        this.voterHistoryNodeHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.signature = signature;
    }

    @Override
    public String toString() {
        return "HistoryNodeVote{" +
                "voterHistoryNodeHash=" + voterHistoryNodeHash +
                ", requestHash=" + requestHash +
                ", isValidRequest=" + isValidRequest +
                ", signature=" + signature +
                '}';
    }
}
