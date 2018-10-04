package io.coti.trustscore.data;

import lombok.Data;

@Data
public class BucketDisputeEventsData extends BucketEventData<DisputeEventData>{

    private static final int periodTime = 60;


    @Override
    protected double getWeight() {
        return 0;
    }

    @Override
    protected double getDecay() {
        return 0;
    }

    @Override
    public int bucketPeriodTime() {
        return periodTime;
    }

    @Override
    protected void addEventToCalculations(DisputeEventData eventData) {

    }

    @Override
    public void ShiftCalculatedTsContribution() {

    }
}
