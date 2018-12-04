package io.coti.trustscore.utils;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TransactionData;
import io.coti.trustscore.data.Buckets.*;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.http.InsertEventRequest;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BucketBuilder {

    private static Map<EventType, Class> bucketMapper = new ConcurrentHashMap<EventType, Class>() {{
        put(EventType.TRANSACTION, BucketTransactionEventsData.class);
        put(EventType.HIGH_FREQUENCY_EVENTS, BucketChargeBackEventsData.class);
        put(EventType.BEHAVIOR_EVENT, BucketBehaviorEventsData.class);
        put(EventType.INITIAL_EVENT, BucketInitialTrustScoreEventsData.class);
        put(EventType.NOT_FULFILMENT_EVENT, BucketNotFulfilmentEventsData.class);
    }};

    public static BucketEventData createBucket(EventType bucketType, UserType userType, Hash userHash) throws IllegalAccessException, InstantiationException {
        BucketEventData bucket = (BucketEventData) bucketMapper.get(bucketType).newInstance();
        bucket.setUserType(userType);
        bucket.setBucketHash(new Hash(ByteBuffer.allocate(userHash.getBytes().length + Integer.BYTES).
                put(userHash.getBytes()).putInt(bucketType.getValue()).array()));
        return bucket;
    }

    public static InsertEventRequest buildTransactionDataRequest(Hash userHash, SignatureData signature, TransactionData transactionData) {
        InsertEventRequest insertEventRequest = new InsertEventRequest();
        insertEventRequest.setUserHash(userHash);
        insertEventRequest.setSignature(signature);
        insertEventRequest.eventType = EventType.TRANSACTION;
        insertEventRequest.setTransactionData(transactionData);
        return insertEventRequest;
    }


}
