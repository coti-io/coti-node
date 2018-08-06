package io.coti.common.data;

import io.coti.common.data.interfaces.IEntity;
import lombok.Data;

import java.util.Date;

@Data
public class TrustScoreData implements IEntity {
    private Hash userHash;
    private Double trustScore;
    private Date createTime;
    private Date lastUpdateTime;

    public TrustScoreData(Hash userHash, double trustScore){
        this.userHash = userHash;
        this.trustScore = trustScore;
        this.createTime = new Date();
        this.lastUpdateTime = new Date();
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