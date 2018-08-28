package unitTest.crypto;

import io.coti.common.data.interfaces.ISignable;

public abstract class SignatureCreationCrypto<T extends ISignable> {
    public abstract byte[] getMessageInBytes(T signable);

    public void signMessage(T signable) {
        signable.setSignerHash(NodeCryptoHelper.getNodeHash());
        signable.setSignature(NodeCryptoHelper.signMessage(this.getMessageInBytes(signable)));
    }
}
