package io.coti.common.data;

import io.coti.common.crypto.CryptoHelper;
import io.coti.common.data.interfaces.IEntity;
import io.coti.common.data.interfaces.ISignValidatable;
import io.coti.common.data.interfaces.ISignable;
import lombok.Data;

import java.nio.ByteBuffer;
import java.util.Date;

import java.security.*;
import java.security.spec.InvalidKeySpecException;

@Data
public class TrustScoreData implements IEntity, ISignValidatable {
    private Hash userHash;
    private Double trustScore;
    private Double kycTrustScore;
    private SignatureData signature;
    private Hash kycServerPublicKey;
    private Date createTime;
    private Date lastUpdateTime;

    public TrustScoreData(Hash userHash, double trustScore) {
        this.userHash = userHash;
        this.trustScore = trustScore;
        this.lastUpdateTime = new Date();
    }

    public TrustScoreData(Hash userHash, double kycTrustScore, SignatureData signature, Hash kycServerPublicKey) {
        this.userHash = userHash;
        this.kycTrustScore = kycTrustScore;
        this.signature = signature;
        this.kycServerPublicKey = kycServerPublicKey;
    }

    @Override
    public boolean verifySignature() {
        try {
            return CryptoHelper.VerifyByPublicKey(this.getMessageInBytes(), signature.getR(), signature.getS(), kycServerPublicKey.toHexString());
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] userHashInBytes = userHash.getBytes();

        ByteBuffer trustScoreBuffer = ByteBuffer.allocate(8);
        trustScoreBuffer.putDouble(kycTrustScore);

        ByteBuffer trustScoreMessageBuffer = ByteBuffer.allocate(userHashInBytes.length + 8).
                put(userHashInBytes).put(trustScoreBuffer.array());

        byte[] trustScoreMessageInBytes = trustScoreMessageBuffer.array();
        byte[] cryptoHashedMessage = CryptoHelper.cryptoHash(trustScoreMessageInBytes).getBytes();
        return cryptoHashedMessage;
    }

    @Override
    public Hash getHash() {
        return userHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.userHash = hash;
    }


}