package io.coti.trustscore.data.scoreevents;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.trustscore.data.scoreenums.FinalScoreType;
import io.coti.trustscore.http.SignedRequest;
import lombok.Data;

@Data
public abstract class SignedScoreData extends ScoreData implements ISignValidatable {

    private static final long serialVersionUID = 2962668135868777563L;
    private Hash signerHash;
    private SignatureData signature;

    public SignedScoreData() {
    }

    public SignedScoreData(SignedRequest request, FinalScoreType finalScoreType) {
        super(request, finalScoreType);
        this.setSignerHash(request.getSignerHash());
        this.setSignature(request.getSignature());
    }

    public SignedScoreData(SignedRequest request, FinalScoreType finalScoreType, Hash hash) {
        super(request, finalScoreType, hash);
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
