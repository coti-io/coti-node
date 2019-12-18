package io.coti.trustscore.data.buckets;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.enums.EventType;
import io.coti.trustscore.data.enums.InitialTrustScoreType;
import io.coti.trustscore.data.events.InitialTrustScoreData;
import io.coti.trustscore.data.events.InitialTrustScoreEventsData;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class BucketInitialTrustScoreEventsData extends BucketEventData<InitialTrustScoreEventsData> implements IEntity {

    private static final long serialVersionUID = -7097339472590357742L;
    private Map<InitialTrustScoreType, InitialTrustScoreData> initialTrustTypeToInitialTrustScoreDataMap;

    public BucketInitialTrustScoreEventsData() {
        super.setEventType(EventType.INITIAL_EVENT);
        initialTrustTypeToInitialTrustScoreDataMap = new ConcurrentHashMap<>();
    }
}
