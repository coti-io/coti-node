package io.coti.basenode.crypto;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public abstract class SignatureCrypto<T extends ISignable & ISignValidatable> extends SignatureCreationCrypto<T> {

    public boolean verifySignature(T signValidatable) {
        try {
            return CryptoHelper.VerifyByPublicKey(this.getSignatureMessage(signValidatable), this.getSignature(signValidatable).getR(), this.getSignature(signValidatable).getS(), this.getSignerHash(signValidatable).toHexString());
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }

    public SignatureData getSignature(T signValidatable) {
        return signValidatable.getSignature();
    }

    public Hash getSignerHash(T signValidatable) {
        return signValidatable.getSignerHash();
    }
}
