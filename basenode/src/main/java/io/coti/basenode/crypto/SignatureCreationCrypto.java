package io.coti.basenode.crypto;

import io.coti.basenode.data.interfaces.ISignable;

public interface SignatureCreationCrypto<T extends ISignable> {

    byte[] getSignatureMessage(T signable);

    default void signMessage(T signable) {
        signable.setSignerHash(NodeCryptoHelper.getNodeHash());
        signable.setSignature(NodeCryptoHelper.signMessage(this.getSignatureMessage(signable)));
    }
}
