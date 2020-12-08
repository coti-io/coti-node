package io.coti.trustscore.utils;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TransactionData;
import io.coti.trustscore.data.Buckets.*;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.EventData;
import io.coti.trustscore.exceptions.BucketBuilderException;
import io.coti.trustscore.http.InsertEventRequest;

import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.Map;

public class BucketBuilder {

    private static final Map<EventType, Class<? extends BucketEventData<? extends EventData>>> bucketMapper = new EnumMap<>(EventType.class);

    static {
        bucketMapper.put(EventType.TRANSACTION, BucketTransactionEventsData.class);
        bucketMapper.put(EventType.HIGH_FREQUENCY_EVENTS, BucketChargeBackEventsData.class);
        bucketMapper.put(EventType.BEHAVIOR_EVENT, BucketBehaviorEventsData.class);
        bucketMapper.put(EventType.INITIAL_EVENT, BucketInitialTrustScoreEventsData.class);
        bucketMapper.put(EventType.NOT_FULFILMENT_EVENT, BucketNotFulfilmentEventsData.class);
    }

    private BucketBuilder() {

    }

    public static BucketEventData<? extends EventData> createBucket(EventType bucketType, UserType userType, Hash userHash) {
        try {
            BucketEventData<? extends EventData> bucket = bucketMapper.get(bucketType).getConstructor().newInstance();
            bucket.setUserType(userType);
            bucket.setBucketHash(new Hash(ByteBuffer.allocate(userHash.getBytes().length + Integer.BYTES).
                    put(userHash.getBytes()).putInt(bucketType.getValue()).array()));
            return bucket;
        } catch (Exception e) {
            throw new BucketBuilderException("Error at creating bucket", e);
        }
    }

    public static InsertEventRequest buildTransactionDataRequest(Hash userHash, SignatureData signature, TransactionData transactionData) {
        InsertEventRequest insertEventRequest = new InsertEventRequest();
        insertEventRequest.setUserHash(userHash);
        insertEventRequest.setSignature(signature);
        insertEventRequest.setEventType(EventType.TRANSACTION);
        insertEventRequest.setTransactionData(transactionData);
        return insertEventRequest;
    }


}
