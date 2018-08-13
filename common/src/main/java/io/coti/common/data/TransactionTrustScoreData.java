package io.coti.common.data;

import io.coti.common.crypto.CryptoHelper;
import io.coti.common.crypto.NodeCryptoHelper;
import io.coti.common.data.interfaces.ISignValidatable;
import io.coti.common.data.interfaces.ISignable;
import lombok.Data;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@Data
public class TransactionTrustScoreData implements Serializable, ISignable, ISignValidatable {
    private Hash userHash;
    private Hash transactionHash;
    private double trustScore;
    private Hash trustScoreNodeHash;
    private SignatureData trustScoreNodeSignature;

    private TransactionTrustScoreData() {
    }

    public TransactionTrustScoreData(Hash userHash, Hash transactionHash, double trustScore) {
        this.userHash = userHash;
        this.transactionHash = transactionHash;
        this.trustScore = trustScore;
    }

    public TransactionTrustScoreData(Hash userHash, Hash transactionHash, double trustScore, Hash trustScoreNodeHash, SignatureData trustScoreNodeSignature) {
        this.userHash = userHash;
        this.transactionHash = transactionHash;
        this.trustScore = trustScore;
        this.trustScoreNodeHash = trustScoreNodeHash;
        this.trustScoreNodeSignature = trustScoreNodeSignature;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        trustScoreNodeHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        trustScoreNodeSignature = signature;
    }

    @Override
    public SignatureData getSignature() {
        return trustScoreNodeSignature;
    }

    @Override
    public Hash getSignerHash() {
        return trustScoreNodeHash;
    }
}
