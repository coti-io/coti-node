package io.coti.trustscore.data.Buckets;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.EventData;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
public abstract class BucketEventData<T extends EventData> implements IEntity {

    private static final long serialVersionUID = -9213788901624395906L;
    private UserType userType;
    private Hash bucketHash;
    private Date startPeriodTime;
    private double calculatedDelta;
    private Map<Hash, T> eventDataHashToEventDataMap;
    private Date lastUpdate;
    private EventType eventType;

    public BucketEventData() {
        eventDataHashToEventDataMap = new HashMap<>();
        startPeriodTime = new Date();
        lastUpdate = new Date();
        calculatedDelta = 0;
    }


    public boolean isEventExistsInBucket(T eventData) {
        return eventDataHashToEventDataMap.containsKey(eventData.getHash());
    }

    public void addEventToBucket(T eventData) {
        if (isEventExistsInBucket(eventData)) {
            return;
        }

        eventDataHashToEventDataMap.put(eventData.getUniqueIdentifier(), eventData);
    }

    @Override
    public Hash getHash() {
        return this.bucketHash;
    }

    @Override
    public void setHash(Hash hash) {
        this.bucketHash = hash;
    }
}

// TODO: delete
