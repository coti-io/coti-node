package io.coti.trustscore.utils;

import io.coti.trustscore.data.Buckets.BucketChargeBackEventsData;
import io.coti.trustscore.data.Buckets.BucketEventData;
import io.coti.trustscore.data.Buckets.BucketTransactionEventsData;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Enums.UserType;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BucketBuilder {

    private static Map<EventType, Class> bucketMapper = new ConcurrentHashMap<EventType, Class>() {{
        put(EventType.TRANSACTION, BucketTransactionEventsData.class);
        put(EventType.HIGH_FREQUENCY_EVENTS, BucketChargeBackEventsData.class);
    }};

    public static BucketEventData createBucket(EventType bucketType, UserType userType) throws IllegalAccessException, InstantiationException {
        BucketEventData bucket = (BucketEventData) bucketMapper.get(bucketType).newInstance();
        bucket.setUserType(userType);
        return bucket;
    }
}
