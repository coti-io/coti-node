package io.coti.trustscore.data.Buckets;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.Enums.BehaviorEventsScoreType;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Events.BehaviorEventsData;
import io.coti.trustscore.data.Events.EventCountAndContributionData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@EqualsAndHashCode(callSuper = true)
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
