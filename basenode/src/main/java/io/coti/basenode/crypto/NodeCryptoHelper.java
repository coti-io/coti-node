package io.coti.basenode.crypto;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NodeCryptoHelper {

    private static String nodePrivateKey;
    private static String nodePublicKey;

    @Value("#{'${global.private.key}'}")
    private void nodePrivateKey(String privateKey) {
        nodePrivateKey = privateKey;
        nodePublicKey = CryptoHelper.GetPublicKeyFromPrivateKey(nodePrivateKey);
    }

    public static SignatureData signMessage(byte[] message) {
        return CryptoHelper.SignBytes(message, nodePrivateKey);
    }

    public static Hash getNodeHash() {
        return new Hash(nodePublicKey);
    }

    public static Hash getNodeAddress() {
        return CryptoHelper.getAddressFromPrivateKey(nodePrivateKey);
    }
}