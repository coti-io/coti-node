package io.coti.trustscore.data.tsbuckets;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.tsenums.UserType;
import io.coti.trustscore.data.tsevents.EventData;
import lombok.Data;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Data
public abstract class BucketData<T extends EventData> implements IEntity {
// Been created using reflection
    private static final long serialVersionUID = -8531799862294211812L;
    private UserType userType;
    private Hash hash;
    private Map<Hash, T> eventDataHashToEventDataMap;
    private LocalDate lastUpdate;

    public BucketData() {
        eventDataHashToEventDataMap = new HashMap<>();
        this.lastUpdate = LocalDate.now(ZoneOffset.UTC);
    }

    private boolean isEventExistsInBucket(T scoreData) {
        return eventDataHashToEventDataMap.containsKey(scoreData.getHash());
    }

    public void addScoreToBucketMap(T scoreData) {

        if (isEventExistsInBucket(scoreData)) {
            return;
        }
        eventDataHashToEventDataMap.put(scoreData.getHash(), scoreData);
    }

    public boolean lastUpdateBeforeToday() {
        return this.getLastUpdate().isBefore(LocalDate.now(ZoneOffset.UTC));
    }

    @Override
    public Hash getHash() {
        return this.hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }

}


