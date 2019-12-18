package io.coti.trustscore.services;

import io.coti.trustscore.config.rules.RulesData;
import io.coti.trustscore.data.buckets.BucketNotFulfilmentEventsData;
import io.coti.trustscore.data.enums.EventType;
import io.coti.trustscore.data.events.NotFulfilmentEventsData;
import io.coti.trustscore.data.events.NotFulfilmentToClientContributionData;
import io.coti.trustscore.services.calculationservices.BucketNotFulfilmentEventsCalculator;
import io.coti.trustscore.services.interfaces.IBucketEventService;
import org.springframework.stereotype.Service;

@Service
public class BucketNotFulfilmentEventsService implements IBucketEventService<NotFulfilmentEventsData, BucketNotFulfilmentEventsData> {

    @Override
    public BucketNotFulfilmentEventsData addEventToCalculations(NotFulfilmentEventsData notFulfilmentEventsData, BucketNotFulfilmentEventsData bucketNotFulfilmentEventsData) {
        // Decay on case that this is the first event today
        BucketNotFulfilmentEventsCalculator bucketCalculator = new BucketNotFulfilmentEventsCalculator(bucketNotFulfilmentEventsData);
        bucketCalculator.decayScores(bucketNotFulfilmentEventsData);

        // Adding event to bucket
        addToBucket(notFulfilmentEventsData, bucketNotFulfilmentEventsData);

        // recalculate bucket after adding the new event.
        bucketCalculator.setCurrentScoresForSpecificClient(notFulfilmentEventsData.getDebtAmount() < 0,
                notFulfilmentEventsData.getClientUserHash());

        return bucketNotFulfilmentEventsData;
    }

    private void addToBucket(NotFulfilmentEventsData notFulfilmentEventsData, BucketNotFulfilmentEventsData bucketNotFulfilmentEventsData) {
        bucketNotFulfilmentEventsData.addEventToBucket(notFulfilmentEventsData);
        // Adding to calculation
        NotFulfilmentToClientContributionData notFulfilmentToClientContributionData
                = bucketNotFulfilmentEventsData.getClientHashToNotFulfilmentContributionMap().get(notFulfilmentEventsData.getClientUserHash());
        if (notFulfilmentToClientContributionData == null) {
            bucketNotFulfilmentEventsData.getClientHashToNotFulfilmentContributionMap()
                    .put(notFulfilmentEventsData.getClientUserHash(),
                            new NotFulfilmentToClientContributionData(notFulfilmentEventsData.getClientUserHash(),
                                    notFulfilmentEventsData.getDebtAmount(),
                                    0,
                                    0));
        } else {
            bucketNotFulfilmentEventsData.getClientHashToNotFulfilmentContributionMap()
                    .put(notFulfilmentEventsData.getClientUserHash(),
                            new NotFulfilmentToClientContributionData(notFulfilmentToClientContributionData.getClientHash(),
                                    notFulfilmentToClientContributionData.getCurrentDebt() + notFulfilmentEventsData.getDebtAmount(),
                                    notFulfilmentToClientContributionData.getFine(),
                                    notFulfilmentToClientContributionData.getTail()));
        }
    }

    @Override
    public EventType getBucketEventType() {
        return EventType.NOT_FULFILMENT_EVENT;
    }

    @Override
    public double getBucketSumScore(BucketNotFulfilmentEventsData bucketNotFulfilmentEventsData) {
        BucketNotFulfilmentEventsCalculator bucketCalculator = new BucketNotFulfilmentEventsCalculator(bucketNotFulfilmentEventsData);
        // Decay on case that this is the first event, or first access to data today
        bucketCalculator.decayScores(bucketNotFulfilmentEventsData);

        return bucketCalculator.getBucketSumScore(bucketNotFulfilmentEventsData);
    }

    public void init(RulesData rulesData) {
        BucketNotFulfilmentEventsCalculator.init(rulesData);
    }

}
