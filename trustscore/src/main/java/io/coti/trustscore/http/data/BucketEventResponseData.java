package io.coti.trustscore.http.data;

import io.coti.basenode.http.data.interfaces.IResponseData;
import io.coti.trustscore.data.Buckets.BucketEventData;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.EventData;
import lombok.Data;

import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class BucketEventResponseData<T extends EventData> implements IResponseData {

    private UserType userType;
    private String bucketHash;
    private Date startPeriodTime;
    private double calculatedDelta;
    private Map<String, T> eventDataHashToEventDataMap;
    private Date lastUpdate;
    private EventType eventType;

    public BucketEventResponseData(BucketEventData<T> bucketEventData) {
        userType = bucketEventData.getUserType();
        bucketHash = bucketEventData.getBucketHash().toString();
        startPeriodTime = bucketEventData.getStartPeriodTime();
        calculatedDelta = bucketEventData.getCalculatedDelta();
        eventDataHashToEventDataMap = bucketEventData.getEventDataHashToEventDataMap().entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().toString(), Map.Entry::getValue));
        lastUpdate = bucketEventData.getLastUpdate();
        eventType = bucketEventData.getEventType();
    }
}
