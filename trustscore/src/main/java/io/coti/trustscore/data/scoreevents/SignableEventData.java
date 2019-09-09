package io.coti.trustscore.data.scoreevents;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.trustscore.http.InsertEventRequest;
import lombok.Data;

@Data
public class SignableEventData extends EventData implements ISignValidatable {

    private static final long serialVersionUID = -5521601577002696387L;
    private Hash eventSignerHash;
    private SignatureData eventSignature;

    public SignableEventData() {
    }

    public SignableEventData(InsertEventRequest request) {
        super(request);
        this.setSignerHash(request.getSignerHash());
        this.setSignatureData(request.getSignature());
    }

    public void setSignatureData(SignatureData eventSignature) {
        this.eventSignature = eventSignature;
    }

    public void setSignerHash(Hash eventSignerHash) {
        this.eventSignerHash = eventSignerHash;
    }

    @Override
    public SignatureData getSignature() {
        return eventSignature;
    }

    @Override
    public Hash getSignerHash() {
        return eventSignerHash;
    }

}

// todo delete