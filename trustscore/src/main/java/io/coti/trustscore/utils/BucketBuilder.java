package io.coti.trustscore.utils;

import io.coti.trustscore.data.BucketDisputeEventsData;
import io.coti.trustscore.data.BucketEventData;
import io.coti.trustscore.data.BucketTransactionEventsData;
import io.coti.trustscore.data.EventType;

import java.util.HashMap;
import java.util.Map;

public class BucketBuilder{

    private static Map<EventType, Class> bucketMapper = new HashMap<EventType, Class>(){{
        put(EventType.TRANSACTION,BucketTransactionEventsData.class);
        put(EventType.DISPUTE,BucketDisputeEventsData.class);
    }};


    public static BucketEventData CreateBucket(EventType bucketType) throws IllegalAccessException, InstantiationException {
        return (BucketEventData)bucketMapper.get(bucketType).newInstance();
    }
}
