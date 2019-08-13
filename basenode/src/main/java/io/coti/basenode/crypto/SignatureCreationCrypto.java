package io.coti.basenode.crypto;

import io.coti.basenode.data.interfaces.ISignable;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class SignatureCreationCrypto<T extends ISignable> {

    @Autowired
    private NodeCryptoHelper nodeCryptoHelper;

    public abstract byte[] getSignatureMessage(T signable);

    public void signMessage(T signable) {
        signable.setSignerHash(nodeCryptoHelper.getNodeHash());
        signable.setSignature(nodeCryptoHelper.signMessage(this.getSignatureMessage(signable)));
    }
}
