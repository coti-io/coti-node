package io.coti.common.data;

import io.coti.common.crypto.CryptoHelper;
import io.coti.common.crypto.NodeCryptoHelper;
import io.coti.common.data.interfaces.ISignValidatable;
import io.coti.common.data.interfaces.ISignable;
import lombok.Data;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@Data
public class TransactionTrustScoreData implements ISignValidatable, ISignable {
    private Hash userHash;
    private Hash transactionHash;
    private double trustScore;
    private Hash trustScoreNodeHash;
    private SignatureData signature;

    public TransactionTrustScoreData(Hash userHash, Hash transactionHash, double trustScore, Hash trustScoreNodeHash) {
        this.userHash = userHash;
        this.transactionHash = transactionHash;
        this.trustScore = trustScore;
        this.trustScoreNodeHash = trustScoreNodeHash;
    }

    public TransactionTrustScoreData(Hash userHash, Hash transactionHash, double trustScore, Hash trustScoreNodeHash, SignatureData signature) {
        this.userHash = userHash;
        this.transactionHash = transactionHash;
        this.trustScore = trustScore;
        this.trustScoreNodeHash = trustScoreNodeHash;
        this.signature = signature;
    }

    @Override
    public boolean verifySignature() {
        try {
            return CryptoHelper.VerifyByPublicKey(this.getMessageInBytes(), signature.getR(), signature.getS(), trustScoreNodeHash.toHexString());
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void signMessage() {

        this.signature = NodeCryptoHelper.signMessage(this.getMessageInBytes());

    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] userHashInBytes = userHash.getBytes();

        byte[] transactionHashInBytes = transactionHash.getBytes();

        ByteBuffer trustScoreBuffer = ByteBuffer.allocate(8);
        trustScoreBuffer.putDouble(trustScore);

        ByteBuffer trustScoreMessageBuffer = ByteBuffer.allocate(userHashInBytes.length + transactionHashInBytes.length + 8).
                put(userHashInBytes).put(transactionHashInBytes).put(trustScoreBuffer.array());

        byte[] trustScoreMessageInBytes = trustScoreMessageBuffer.array();
        byte[] cryptoHashedMessage = CryptoHelper.cryptoHash(trustScoreMessageInBytes).getBytes();
        return cryptoHashedMessage;
    }

}
