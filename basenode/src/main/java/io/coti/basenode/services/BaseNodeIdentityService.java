package io.coti.basenode.services;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.exceptions.NodeCryptoException;
import io.coti.basenode.services.interfaces.INodeIdentityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static io.coti.basenode.services.BaseNodeServiceManager.secretManagerService;

@Service
@Slf4j
public class BaseNodeIdentityService implements INodeIdentityService {

    @Value("#{'${global.private.key:}'}")
    private String nodePrivateKey;
    @Value("#{'${secret.private.key.name:}'}")
    private String nodePrivateKeySecretName;
    private String seed;
    private String nodePublicKey;

    public void init() {
        nodePrivateKey = secretManagerService.getSecret(nodePrivateKey, nodePrivateKeySecretName, "Private Key");
        nodePublicKey = CryptoHelper.getPublicKeyFromPrivateKey(nodePrivateKey);
        log.info("Node public key is set to {}", nodePublicKey);
    }

    public SignatureData signMessage(byte[] message) {
        return CryptoHelper.signBytes(message, nodePrivateKey);
    }

    public SignatureData signMessage(byte[] message, Integer index) {
        return CryptoHelper.signBytes(message, CryptoHelper.generatePrivateKey(seed, index).toHexString());
    }

    public Hash generateAddress(Integer index) {
        if (seed == null) {
            throw new NodeCryptoException("Seed is not set");
        }
        return CryptoHelper.generateAddress(seed, index);
    }

    public Hash generateAddress(String seed, Integer index) {
        setSeed(seed);
        return generateAddress(index);
    }

    public Hash getNodeHash() {
        return new Hash(nodePublicKey);
    }

    public void setSeed(String seedInput) {
        if (seedInput == null) {
            throw new NodeCryptoException("Seed can not be set to null");
        }
        if (seed == null) {
            seed = seedInput;
        }
    }

}
