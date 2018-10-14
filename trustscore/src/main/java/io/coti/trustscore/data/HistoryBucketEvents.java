package io.coti.trustscore.data;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.Buckets.BucketEventData;
import io.coti.trustscore.data.Enums.EventType;

import java.util.List;
import java.util.Map;

public class HistoryBucketEvents implements IEntity {

    private Hash userHash;
    private Map<EventType, List<BucketEventData>> historyBucketsEvents;


    @Override
    public Hash getHash() {
        return userHash;
    }

    @Override
    public void setHash(Hash hash) {
        userHash = hash;
    }
}
