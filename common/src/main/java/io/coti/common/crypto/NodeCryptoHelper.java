package io.coti.common.crypto;

import io.coti.common.data.Hash;
import io.coti.common.data.SignatureData;
import io.coti.common.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;


@Slf4j
@Service
public class NodeCryptoHelper {

    private static String nodePublicKey;
    private static String nodePrivateKey;

    @Value("#{'${global.private.key}'}")
    public void setNodePublicKey(String nodePrivateKey)
    {
        this.nodePrivateKey = nodePrivateKey;
        nodePublicKey = CryptoHelper.GetPublicKeyFromPrivateKey(nodePrivateKey);
    }

    public void setNodeHashAndSignature(TransactionData transactionData)
    {
        transactionData.setNodeHash(new Hash(nodePublicKey));
        SignatureData signatureData = CryptoHelper.SignBytes(transactionData.getHash().getBytes(), nodePrivateKey);
        transactionData.setNodeSignature(signatureData);
    }

    public boolean verifyTransactionSignature(TransactionData transactionData) throws InvalidKeySpecException, NoSuchAlgorithmException {
        String publicKey = transactionData.getNodeHash().toHexString();
        boolean resultVerify = CryptoHelper.VerifyByPublicKey(transactionData.getHash().getBytes(),transactionData.getNodeSignature().getR(), transactionData.getNodeSignature().getS(), publicKey);
        return resultVerify;

    }
}
