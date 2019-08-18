package io.coti.basenode.crypto;

import io.coti.basenode.constants.BaseNodeApplicationConstant;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import static io.coti.basenode.constants.BaseNodeApplicationConstant.NODE_PRIVATE_KEY;

@Service
public class NodeCryptoHelper {

    @Autowired
    private static BaseNodeApplicationConstant applicationConstant;
    private static String NODE_PUBLIC_KEY;
    private static String seed;
    @Autowired
    private static ApplicationContext applicationContext;

    private NodeCryptoHelper() {
        NODE_PUBLIC_KEY = CryptoHelper.getPublicKeyFromPrivateKey(((BaseNodeApplicationConstant) applicationContext.getBean(BaseNodeApplicationConstant.class)).NODE_PRIVATE_KEY);
    }

    public static SignatureData signMessage(byte[] message) {
        return CryptoHelper.signBytes(message, NODE_PRIVATE_KEY);
    }

    public static SignatureData signMessage(byte[] message, Integer index) {
        return CryptoHelper.signBytes(message, CryptoHelper.generatePrivateKey(seed, index).toHexString());
    }

    public Hash generateAddress(String seed, Integer index) {
        if (this.seed == null) {
            this.seed = seed;
        }
        return CryptoHelper.generateAddress(seed, index);
    }

    public static Hash getNodeHash() {
        return new Hash(NODE_PUBLIC_KEY);
    }

    public static Hash getNodeAddress() {
        return CryptoHelper.getAddressFromPrivateKey(NODE_PRIVATE_KEY);
    }
}