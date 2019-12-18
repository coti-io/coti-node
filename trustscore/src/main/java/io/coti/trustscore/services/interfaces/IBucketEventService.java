package io.coti.trustscore.services.interfaces;

import io.coti.trustscore.data.buckets.BucketEventData;
import io.coti.trustscore.data.enums.EventType;
import io.coti.trustscore.data.events.EventData;

public interface IBucketEventService<T extends EventData, S extends BucketEventData> {
    S addEventToCalculations(T eventData, S bucketEventsData);

    double getBucketSumScore(S bucketEventsData);

    EventType getBucketEventType();
}
