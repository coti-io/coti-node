package io.coti.trustscore.data.Buckets;

import io.coti.basenode.data.Hash;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.EventData;
import io.coti.trustscore.utils.DatesCalculation;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public abstract class BucketEventData<T extends EventData> implements Serializable {

    private Date startPeriodTime;
    private double calculatedDelta;
    private UserType userType;
    private Map<Hash, EventData> eventDataHashToEventDataMap;
    private Date lastUpdate;

    public BucketEventData() {
        eventDataHashToEventDataMap = new LinkedHashMap<>();
        startPeriodTime = new Date();
        lastUpdate = new Date();
        calculatedDelta = 0;
    }


    public boolean isEventExistsInBucket(T eventData) {
        return (eventDataHashToEventDataMap.containsKey(eventData.getHash()));
    }

    public void addEventToBucket(T eventData) {
        if (isEventExistsInBucket(eventData)) {
            return;
        }
        //TODO: if we have a problem here, event can be added without calculated
        eventDataHashToEventDataMap.put(eventData.getUniqueIdentifier(), eventData);
    }

    public boolean lastUpdateBeforeToday() {
        return this.getLastUpdate().before(DatesCalculation.setDateOnBeginningOfDay(new Date()));
    }
}


