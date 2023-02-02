package io.coti.basenode.crypto;

import io.coti.basenode.data.interfaces.ISignable;

import static io.coti.basenode.services.BaseNodeServiceManager.nodeIdentityService;

public interface SignatureCreationCrypto<T extends ISignable> {

    byte[] getSignatureMessage(T signable);

    default void signMessage(T signable) {
        signable.setSignerHash(nodeIdentityService.getNodeHash());
        signable.setSignature(nodeIdentityService.signMessage(this.getSignatureMessage(signable)));
    }
}
