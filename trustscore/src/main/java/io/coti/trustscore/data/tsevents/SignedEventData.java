package io.coti.trustscore.data.tsevents;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.trustscore.http.SignedRequest;
import lombok.Data;

@Data
public abstract class SignedEventData extends EventData implements ISignValidatable {

    private static final long serialVersionUID = 2962668135868777563L;
    private Hash signerHash;
    private SignatureData signature;

    public SignedEventData() {
    }

    public SignedEventData(SignedRequest request) {
        super(request);
        this.setSignerHash(request.getSignerHash());
        this.setSignature(request.getSignature());
    }

// Been created using reflection
    public SignedEventData(SignedRequest request, Hash hash) {
        super(hash);
        this.setSignerHash(request.getSignerHash());
        this.setSignature(request.getSignature());
    }

    @Override
    public SignatureData getSignature() {
        return signature;
    }

    @Override
    public Hash getSignerHash() {
        return signerHash;
    }

    public void setSignerHash(Hash eventSignerHash) {
        this.signerHash = eventSignerHash;
    }

}
