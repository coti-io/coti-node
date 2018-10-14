package io.coti.trustscore.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.trustscore.data.Buckets.BucketEventData;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Events.EventData;
import io.coti.trustscore.utils.BucketBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Data
public class TrustScoreData implements IEntity, ISignValidatable {
    private Hash userHash;
    private Double trustScore;
    private Double kycTrustScore;
    private SignatureData signature;
    private Hash kycServerPublicKey;
    private Date createTime;
    private Date lastUpdateTime;
    private HashMap<EventType, BucketEventData> lastBucketEventData;
    private HashMap<EventType, List<Double>> bucketsHistoryCalculations;

    public TrustScoreData(Hash userHash, double trustScore) {
        this.userHash = userHash;
        this.trustScore = trustScore;
        this.lastUpdateTime = new Date();
    }

    public double getCurrentTs() {
        if (shouldRecalculate())
            calculateTsOnNewDate();
        return trustScore;
    }

    private boolean shouldRecalculate() {

        //TODO: need to change here to check date
        return true;
    }

    private void calculateTsOnNewDate() {
        DoShiftToBuckets();
        double currentTsValue = trustScore;
        for (Map.Entry<EventType, BucketEventData> entry : lastBucketEventData.entrySet()) {
            currentTsValue += calculateBucketTypeMagnitude(bucketsHistoryCalculations.get(entry.getKey())) + entry.getValue().getCalculatedDelta();
        }
        trustScore = currentTsValue;
    }

    private double calculateBucketTypeMagnitude(List<Double> listOfEvents) {
        int length = listOfEvents.size();
        double sumValue = listOfEvents.stream().mapToDouble(Double::doubleValue).sum();
        return sumValue / length;
    }

    public void DoShiftToBuckets() {

    }
    public void addEvent(EventData event) {

        if (!lastBucketEventData.containsKey(event.getEventType())) {
            try {
                lastBucketEventData.put(event.getEventType(), BucketBuilder.CreateBucket(event.getEventType()));
            } catch (IllegalAccessException | InstantiationException e) {
                log.error("error while trying create a bucket", e);
            }
        }
        BucketEventData lastBucket = lastBucketEventData.get(event.getEventType());
        lastBucket.addEventToBucket(event);
    }

    public TrustScoreData(Hash userHash, double kycTrustScore, SignatureData signature, Hash kycServerPublicKey) {
        this.userHash = userHash;
        this.kycTrustScore = kycTrustScore;
        this.signature = signature;
        this.kycServerPublicKey = kycServerPublicKey;
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