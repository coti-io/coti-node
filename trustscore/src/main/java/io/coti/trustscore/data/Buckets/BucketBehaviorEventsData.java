package io.coti.trustscore.data.Buckets;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.Enums.BehaviorEventsScoreType;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.BehaviorEventsData;
import io.coti.trustscore.data.Events.EventCountAndContributionData;
import lombok.Data;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class BucketBehaviorEventsData extends BucketEventData<BehaviorEventsData> implements IEntity {
    private UserType userType;
    private Hash bucketHash;
    private Map<BehaviorEventsScoreType, Map<Date, Double>> behaviorEventTypeToOldEventsContributionMap;
    private Map<BehaviorEventsScoreType, EventCountAndContributionData> behaviorEventTypeToCurrentEventCountAndContributionDataMap;
    private Map<BehaviorEventsScoreType, Double> behaviorEventTypeToTotalEventsContributionMap;

    public BucketBehaviorEventsData() {
        behaviorEventTypeToOldEventsContributionMap = new ConcurrentHashMap<>();
        behaviorEventTypeToCurrentEventCountAndContributionDataMap = new ConcurrentHashMap<>();
        behaviorEventTypeToTotalEventsContributionMap = new ConcurrentHashMap<>();
    }

    @Override
    public Hash getHash() {
        return this.bucketHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.bucketHash = hash;
    }

}
