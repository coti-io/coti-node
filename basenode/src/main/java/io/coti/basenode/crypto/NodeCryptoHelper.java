package io.coti.basenode.crypto;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class NodeCryptoHelper {

    @Value("#{'${global.private.key}'}")
    private String nodePrivateKey;
    private static String nodePublicKey;

    @PostConstruct
    public void init(){
        nodePublicKey = CryptoHelper.GetPublicKeyFromPrivateKey(nodePrivateKey);
    }

    public SignatureData signMessage(byte[] message) {
        return CryptoHelper.SignBytes(message, nodePrivateKey);
    }

    public static Hash getNodeHash() {
        return new Hash(nodePublicKey);
    }
}