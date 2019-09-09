package io.coti.trustscore.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Enums.UserType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Data
public class TrustScoreData implements IEntity, ISignValidatable {

    private static final long serialVersionUID = -8077752791984878796L;
    private Hash userHash;
    private Double kycTrustScore;
    private SignatureData signature;
    private Hash kycServerPublicKey;
    private Date createTime;
    private ConcurrentHashMap<EventType, Hash> eventTypeToBucketHashMap;
    private UserType userType;
    private Boolean zeroTrustFlag;

    public TrustScoreData(Hash userHash, double kycTrustScore, SignatureData signature, Hash kycServerPublicKey, UserType userType) {
        this.userHash = userHash;
        this.kycTrustScore = kycTrustScore;
        this.signature = signature;
        this.kycServerPublicKey = kycServerPublicKey;
        this.userType = userType;
        this.createTime = new Date();
        this.zeroTrustFlag = false;

        eventTypeToBucketHashMap = new ConcurrentHashMap<>();
    }

    @Override
    public Hash getHash() {
        return userHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.userHash = hash;
    }


    @Override
    public SignatureData getSignature() {
        return signature;
    }

    @Override
    public Hash getSignerHash() {
        return kycServerPublicKey;
    }

}

//todo delete it