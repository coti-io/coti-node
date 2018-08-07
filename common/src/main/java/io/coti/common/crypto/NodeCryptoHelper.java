package io.coti.common.crypto;

import io.coti.common.communication.DspVote;
import io.coti.common.data.Hash;
import io.coti.common.data.SignatureData;
import io.coti.common.data.TransactionData;
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

    public static void setNodeHashAndSignature(DspVote dspVote) {
        dspVote.setVoterDspHash(new Hash(nodePublicKey));
        SignatureData signatureData = CryptoHelper.SignBytes(dspVote.getHash().getBytes(), nodePrivateKey);
        dspVote.setSignature(signatureData);
    }

    public static void setNodeHashAndSignature(TransactionData transactionData) {
        transactionData.setNodeHash(new Hash(nodePublicKey));
        SignatureData signatureData = CryptoHelper.SignBytes(transactionData.getHash().getBytes(), nodePrivateKey);
        transactionData.setNodeSignature(signatureData);
    }

    public static SignatureData signMessage(byte[] message) {
        return CryptoHelper.SignBytes(message, nodePublicKey);
    }

    public static Hash getNodeHash() {
        return new Hash(nodePublicKey);
    }
}