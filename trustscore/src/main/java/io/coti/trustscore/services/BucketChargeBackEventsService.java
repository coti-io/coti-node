package io.coti.trustscore.services;

import io.coti.basenode.data.TransactionData;
import io.coti.trustscore.config.rules.RulesData;
import io.coti.trustscore.data.buckets.BucketChargeBackEventsData;
import io.coti.trustscore.data.enums.EventType;
import io.coti.trustscore.data.events.ChargeBackEventsData;
import io.coti.trustscore.services.calculationservices.BucketChargeBackEventsCalculator;
import io.coti.trustscore.services.interfaces.IBucketEventService;
import lombok.Data;
import org.springframework.stereotype.Service;

@Data
@Service
public class BucketChargeBackEventsService implements IBucketEventService<ChargeBackEventsData, BucketChargeBackEventsData> {

    @Override
    public BucketChargeBackEventsData addEventToCalculations(ChargeBackEventsData chargeBackEventsData,
                                                             BucketChargeBackEventsData bucketChargeBackEventsData) {

        // Decay on case that this is the first event today
        BucketChargeBackEventsCalculator bucketCalculator = new BucketChargeBackEventsCalculator(bucketChargeBackEventsData);
        bucketCalculator.decayScores(bucketChargeBackEventsData);

        // Adding the new event
        addToBucket(chargeBackEventsData, bucketChargeBackEventsData);

        // recalculate bucket after adding the new event.
        bucketCalculator.setCurrentScores();

        return bucketChargeBackEventsData;

    }

    public void addPaymentTransactionToCalculations(TransactionData transactionData, BucketChargeBackEventsData bucketChargeBackEventsData) {

        // Decay on case that this is the first event, or first access to data today
        BucketChargeBackEventsCalculator bucketCalculator = new BucketChargeBackEventsCalculator(bucketChargeBackEventsData);
        bucketCalculator.decayScores(bucketChargeBackEventsData);

        // Adding credit to bucket
        bucketChargeBackEventsData.getCurrentDatePaymentTransactions().put(transactionData.getHash(), transactionData.getAmount().doubleValue());

        // recalculate bucket after adding the new event.
        bucketCalculator.setCurrentScores();
    }

    @Override
    public double getBucketSumScore(BucketChargeBackEventsData bucketChargeBackEventsData) {
        BucketChargeBackEventsCalculator bucketCalculator = new BucketChargeBackEventsCalculator(bucketChargeBackEventsData);
        // Decay on case that this is the first event, or first access to data today
        if (bucketCalculator.decayScores(bucketChargeBackEventsData)) {
            bucketCalculator.setCurrentScores();
        }
        return bucketCalculator.getBucketSumScore(bucketChargeBackEventsData);
    }

    @Override
    public EventType getBucketEventType() {
        return EventType.HIGH_FREQUENCY_EVENTS;
    }

    public void init(RulesData rulesData) {
        BucketChargeBackEventsCalculator.init(rulesData);
    }

    private void addToBucket(ChargeBackEventsData chargeBackEventsData,
                             BucketChargeBackEventsData bucketChargeBackEventsData) {

        bucketChargeBackEventsData.addEventToBucket(chargeBackEventsData);
        bucketChargeBackEventsData.getCurrentDateChargeBacks().put(chargeBackEventsData.getHash(),
                chargeBackEventsData.getTransactionData().getAmount().doubleValue());
    }
}
