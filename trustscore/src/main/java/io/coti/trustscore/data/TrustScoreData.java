package io.coti.trustscore.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.trustscore.data.Buckets.BucketEventData;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.EventData;
import io.coti.trustscore.utils.BucketBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Data
public class TrustScoreData implements IEntity, ISignValidatable {
    private Hash userHash;
    private Double kycTrustScore;
    private SignatureData signature;
    private Hash kycServerPublicKey;
    private Date createTime;
    private ConcurrentHashMap<EventType, BucketEventData> lastBucketEventData;
    private ConcurrentHashMap<EventType, List<Double>> bucketsHistoryCalculations;
    private UserType userType;

    public TrustScoreData(Hash userHash, double kycTrustScore, SignatureData signature, Hash kycServerPublicKey, UserType userType) {
        this.userHash = userHash;
        this.kycTrustScore = kycTrustScore;
        this.signature = signature;
        this.kycServerPublicKey = kycServerPublicKey;
        this.userType = userType;
        this.createTime = new Date();

        lastBucketEventData = new ConcurrentHashMap<>();
        bucketsHistoryCalculations = new ConcurrentHashMap<>();
        for (EventType event : EventType.values()) {
            try {
                lastBucketEventData.put(event, BucketBuilder.createBucket(event, userType));
            } catch (IllegalAccessException | InstantiationException e) {
                log.error(e.toString());
            }
        }

    }

    public void addEvent(EventData event) {

        if (!lastBucketEventData.containsKey(event.getEventType())) {
            try {
                lastBucketEventData.put(event.getEventType(), BucketBuilder.createBucket(event.getEventType(), this.userType));
            } catch (IllegalAccessException | InstantiationException e) {
                log.error("error while trying create a bucket", e);
            }
        }
        BucketEventData lastBucket = lastBucketEventData.get(event.getEventType());
        lastBucket.addEventToBucket(event);
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