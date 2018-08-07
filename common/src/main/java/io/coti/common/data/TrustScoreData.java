package io.coti.common.data;

import io.coti.common.data.interfaces.IEntity;
import lombok.Data;

import java.util.Date;

@Data
public class TrustScoreData implements IEntity {
    private Hash userHash;
    private Double trustScore;
    private Double kycTrustScore;
    private SignatureData signature;
    private Date createTime;
    private Date lastUpdateTime;

    public TrustScoreData(Hash userHash, double trustScore){
        this.userHash = userHash;
        this.trustScore = trustScore;
        this.lastUpdateTime = new Date();
    }

    public TrustScoreData(Hash userHash, double kycTrustScore, SignatureData signature){
        this.userHash = userHash;
        this.kycTrustScore = kycTrustScore;
        this.signature = signature;
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