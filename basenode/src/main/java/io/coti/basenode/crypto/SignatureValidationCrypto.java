package io.coti.basenode.crypto;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.ISignValidatable;
import lombok.extern.slf4j.Slf4j;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@Slf4j
public abstract class SignatureValidationCrypto<T extends ISignValidatable> {

    public abstract byte[] getSignatureMessage(T signValidatable);

    public boolean verifySignature(T signValidatable) {
        try {
            return CryptoHelper.verifyByPublicKey(this.getSignatureMessage(signValidatable), this.getSignature(signValidatable).getR(), this.getSignature(signValidatable).getS(), this.getSignerHash(signValidatable).toHexString());
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            log.error("Verify signature error: {} {}", e.getClass(), e.getMessage());
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
