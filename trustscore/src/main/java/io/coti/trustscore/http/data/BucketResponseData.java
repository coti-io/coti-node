package io.coti.trustscore.http.data;

import io.coti.basenode.data.Hash;
import io.coti.trustscore.data.tsbuckets.BucketData;
import io.coti.trustscore.data.tsenums.UserType;
import io.coti.trustscore.data.tsevents.EventData;
import lombok.Data;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Data
public class BucketResponseData<T extends EventData> implements Serializable {
    private UserType userType;
    private String hash;
    private Map<Hash, T> eventDataHashToEventDataMap;
    private String lastUpdate;

    public BucketResponseData(BucketData bucketData) {
        this.userType = bucketData.getUserType();
        this.hash = bucketData.getHash().toString();

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        this.lastUpdate = bucketData.getLastUpdate().format(formatter);

        this.eventDataHashToEventDataMap = bucketData.getEventDataHashToEventDataMap();

    }
}


