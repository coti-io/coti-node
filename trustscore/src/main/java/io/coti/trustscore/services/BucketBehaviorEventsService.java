package io.coti.trustscore.services;

import io.coti.trustscore.services.calculationServices.BucketBehaviorEventsCalculator;
import io.coti.trustscore.services.calculationServices.BucketCalculator;
import io.coti.trustscore.data.Buckets.BucketBehaviorEventsData;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Events.BehaviorEventsData;
import io.coti.trustscore.data.Events.EventCountAndContributionData;
import io.coti.trustscore.config.rules.RulesData;
import io.coti.trustscore.services.interfaces.IBucketEventService;

public class BucketBehaviorEventsService implements IBucketEventService<BehaviorEventsData, BucketBehaviorEventsData> {

    @Override
    public BucketBehaviorEventsData addEventToCalculations(BehaviorEventsData behaviorEventsData, BucketBehaviorEventsData bucketBehaviorEventsData) {
        // Decay on case that this is the first event today
        BucketBehaviorEventsCalculator bucketCalculator = new BucketBehaviorEventsCalculator(bucketBehaviorEventsData);
        bucketCalculator.decayScores(bucketBehaviorEventsData);

        // Adding event to bucket
        addToBucket(behaviorEventsData, bucketBehaviorEventsData);

        // recalculate bucket after adding the new event.
        bucketCalculator.setCurrentScores();

        return bucketBehaviorEventsData;
    }

    private void addToBucket(BehaviorEventsData behaviorEventsData, BucketBehaviorEventsData bucketBehaviorEventsData) {
        bucketBehaviorEventsData.addEventToBucket(behaviorEventsData);

        // Adding to calculation
        EventCountAndContributionData eventCountAndContributionData
                = bucketBehaviorEventsData.getBehaviorEventTypeToCurrentEventCountAndContributionDataMap().get(behaviorEventsData.getBehaviorEventsScoreType());
        if (eventCountAndContributionData == null) {
            bucketBehaviorEventsData.getBehaviorEventTypeToCurrentEventCountAndContributionDataMap()
                    .put(behaviorEventsData.getBehaviorEventsScoreType(), new EventCountAndContributionData(1, 0));
        } else {
            bucketBehaviorEventsData.getBehaviorEventTypeToCurrentEventCountAndContributionDataMap()
                    .put(behaviorEventsData.getBehaviorEventsScoreType(),
                            new EventCountAndContributionData(eventCountAndContributionData.getCount() + 1, eventCountAndContributionData.getContribution()));
        }
    }

    @Override
    public EventType getBucketEventType() {
        return EventType.BEHAVIOR_EVENT;
    }

    @Override
    public double getBucketSumScore(BucketBehaviorEventsData bucketBehaviorEventsData) {
        BucketCalculator bucketCalculator = new BucketBehaviorEventsCalculator(bucketBehaviorEventsData);
        // Decay on case that this is the first event, or first access to data today
        if (bucketCalculator.decayScores(bucketBehaviorEventsData)) {
            bucketCalculator.setCurrentScores();
        }
        return ((BucketBehaviorEventsCalculator) bucketCalculator).getBucketSumScore(bucketBehaviorEventsData);
    }

    public void init(RulesData rulesData) {
        BucketBehaviorEventsCalculator.init(rulesData);
    }

}
