package io.coti.trustscore.utils;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TransactionData;
import io.coti.trustscore.data.buckets.*;
import io.coti.trustscore.data.enums.EventType;
import io.coti.trustscore.data.enums.UserType;
import io.coti.trustscore.http.InsertEventRequest;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BucketBuilder {

    private static Map<EventType, Class> bucketMapper = new ConcurrentHashMap<>();

    static {
        bucketMapper.put(EventType.TRANSACTION, BucketTransactionEventsData.class);
        bucketMapper.put(EventType.HIGH_FREQUENCY_EVENTS, BucketChargeBackEventsData.class);
        bucketMapper.put(EventType.BEHAVIOR_EVENT, BucketBehaviorEventsData.class);
        bucketMapper.put(EventType.INITIAL_EVENT, BucketInitialTrustScoreEventsData.class);
        bucketMapper.put(EventType.NOT_FULFILMENT_EVENT, BucketNotFulfilmentEventsData.class);
    }

    private BucketBuilder() {
        throw new IllegalStateException("Utility class");
    }

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
