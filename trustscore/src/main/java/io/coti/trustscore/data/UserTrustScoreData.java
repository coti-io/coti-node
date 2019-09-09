package io.coti.trustscore.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.scoreenums.ScoreType;
import io.coti.trustscore.data.scoreenums.UserType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Data
public class UserTrustScoreData implements IEntity {

    private static final long serialVersionUID = -878589465719733703L;
    private Hash hash;
    private LocalDateTime createTime;
    private ConcurrentHashMap<ScoreType, Hash> eventTypeToBucketHashMap;
    private UserType userType;
    private Boolean zeroTrustFlag;

    public UserTrustScoreData(Hash hash, UserType userType) {
        this.hash = hash;
        this.userType = userType;
        this.createTime = LocalDateTime.now(ZoneOffset.UTC);
        this.zeroTrustFlag = false;

        eventTypeToBucketHashMap = new ConcurrentHashMap<>();
    }

    public UserTrustScoreData(Hash hash, String userType) {
        this.hash = hash;
        this.userType = UserType.enumFromString(userType);
        this.createTime = LocalDateTime.now(ZoneOffset.UTC);
        this.zeroTrustFlag = false;

        eventTypeToBucketHashMap = new ConcurrentHashMap<>();
    }

    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }

}