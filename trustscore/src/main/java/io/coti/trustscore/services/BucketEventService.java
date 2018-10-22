package io.coti.trustscore.services;

import io.coti.trustscore.data.Buckets.BucketEventData;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Events.EventData;

public interface BucketEventService<T extends EventData, S extends BucketEventData> {
    S addEventToCalculations(T eventData, S bucketEventsData);

    double getBucketSumScore(S bucketEventsData);

    EventType getBucketEventType();
}
