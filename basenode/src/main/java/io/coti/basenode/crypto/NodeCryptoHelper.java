package io.coti.basenode.crypto;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NodeCryptoHelper {

    private static String nodePrivateKey;
    private static String nodePublicKey;
    private static String seed;

    private NodeCryptoHelper() {
    }

    @SuppressWarnings("unused")
    private static void nodePrivateKey(String privateKey) {
        if (nodePrivateKey == null) {
            nodePrivateKey = privateKey;
            nodePublicKey = CryptoHelper.getPublicKeyFromPrivateKey(nodePrivateKey);
            log.info("Node public key is set to {}", nodePublicKey);
        }
    }

    public static SignatureData signMessage(byte[] message) {
        return CryptoHelper.signBytes(message, nodePrivateKey);
    }

    public static SignatureData signMessage(byte[] message, Integer index) {
        return CryptoHelper.signBytes(message, CryptoHelper.generatePrivateKey(seed, index).toHexString());
    }

    public static Hash generateAddress(String seed, Integer index) {
        if (NodeCryptoHelper.seed == null) {
            NodeCryptoHelper.seed = seed;
        }
        return CryptoHelper.generateAddress(NodeCryptoHelper.seed, index);
    }

    public static Hash getNodeHash() {
        return new Hash(nodePublicKey);
    }

}
