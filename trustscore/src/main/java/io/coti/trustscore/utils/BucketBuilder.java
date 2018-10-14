package io.coti.trustscore.utils;

import io.coti.trustscore.data.Buckets.BucketDisputeEventsData;
import io.coti.trustscore.data.Buckets.BucketEventData;
import io.coti.trustscore.data.Buckets.BucketTransactionEventsData;
import io.coti.trustscore.data.Enums.EventType;

import java.util.HashMap;
import java.util.Map;

public class BucketBuilder {

    private static Map<EventType, Class> bucketMapper = new HashMap<EventType, Class>() {{
        put(EventType.TRANSACTION, BucketTransactionEventsData.class);
        put(EventType.DISPUTE, BucketDisputeEventsData.class);
    }};


    public static BucketEventData CreateBucket(EventType bucketType) throws IllegalAccessException, InstantiationException {
        return (BucketEventData) bucketMapper.get(bucketType).newInstance();
    }
}
