package io.coti.trustscore.data.buckets;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.enums.BehaviorEventsScoreType;
import io.coti.trustscore.data.enums.EventType;
import io.coti.trustscore.data.events.BehaviorEventsData;
import io.coti.trustscore.data.events.EventCountAndContributionData;
import lombok.Data;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class BucketBehaviorEventsData extends BucketEventData<BehaviorEventsData> implements IEntity {

    private static final long serialVersionUID = 2383844692698551161L;
    private Map<BehaviorEventsScoreType, Map<Date, Double>> behaviorEventTypeToOldEventsContributionMap;
    private Map<BehaviorEventsScoreType, EventCountAndContributionData> behaviorEventTypeToCurrentEventCountAndContributionDataMap;
    private Map<BehaviorEventsScoreType, Double> behaviorEventTypeToTotalEventsContributionMap;

    public BucketBehaviorEventsData() {
        super.setEventType(EventType.BEHAVIOR_EVENT);
        behaviorEventTypeToOldEventsContributionMap = new ConcurrentHashMap<>();
        behaviorEventTypeToCurrentEventCountAndContributionDataMap = new ConcurrentHashMap<>();
        behaviorEventTypeToTotalEventsContributionMap = new ConcurrentHashMap<>();
    }

}
