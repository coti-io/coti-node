package io.coti.common.crypto;

import io.coti.common.data.DspVote;
import io.coti.common.data.Hash;
import io.coti.common.data.SignatureData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NodeCryptoHelper {

    private static String nodePublicKey;
    private static String nodePrivateKey;

    @Value("#{'${global.private.key}'}")
    public void setNodePublicKey(String nodePrivateKey) {
        this.nodePrivateKey = nodePrivateKey;
        nodePublicKey = CryptoHelper.GetPublicKeyFromPrivateKey(nodePrivateKey);
    }

    public static SignatureData signMessage(byte[] message) {
        return CryptoHelper.SignBytes(message, nodePrivateKey);
    }

    public static Hash getNodeHash() {
        return new Hash(nodePublicKey);
    }
}