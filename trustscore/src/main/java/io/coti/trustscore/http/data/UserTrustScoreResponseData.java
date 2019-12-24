package io.coti.trustscore.http.data;

import io.coti.basenode.data.Hash;
import io.coti.trustscore.data.UserTrustScoreData;
import io.coti.trustscore.data.tsenums.EventType;
import io.coti.trustscore.data.tsenums.UserType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Data
public class UserTrustScoreResponseData implements Serializable {
    private String hash;
    private String createTime;
    private ConcurrentHashMap<EventType, String> eventTypeToBucketHashMap;
    private UserType userType;
    private Boolean zeroTrustFlag;

    public UserTrustScoreResponseData(UserTrustScoreData userTrustScoreData) {
        this.hash = userTrustScoreData.getHash().toString();
        this.userType = userTrustScoreData.getUserType();
        this.zeroTrustFlag = userTrustScoreData.getZeroTrustFlag();

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        this.createTime = userTrustScoreData.getCreateTime().format(formatter);

        eventTypeToBucketHashMap = new ConcurrentHashMap<>();
        for (ConcurrentHashMap.Entry<EventType, Hash> eventType : userTrustScoreData.getEventTypeToBucketHashMap().entrySet()) {
            eventTypeToBucketHashMap.put(eventType.getKey(), eventType.getValue().toString());
        }

    }


}