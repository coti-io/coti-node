package io.coti.trustscore.data.Buckets;

import io.coti.trustscore.data.Events.DisputeEventData;
import lombok.Data;

@Data
public class BucketDisputeEventsData extends BucketEventData<DisputeEventData> {

    private static final int periodTime = 60;

    @Override
    public int bucketPeriodTime() {
        return periodTime;
    }

    @Override
    protected void addEventToCalculations(DisputeEventData eventData) {

    }

}
