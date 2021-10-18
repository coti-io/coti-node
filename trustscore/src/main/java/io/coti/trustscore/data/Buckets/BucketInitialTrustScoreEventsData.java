package io.coti.trustscore.data.Buckets;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Enums.InitialTrustScoreType;
import io.coti.trustscore.data.Events.InitialTrustScoreData;
import io.coti.trustscore.data.Events.InitialTrustScoreEventsData;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@EqualsAndHashCode(callSuper = true)
public class BucketInitialTrustScoreEventsData extends BucketEventData<InitialTrustScoreEventsData> implements IEntity {

    private static final long serialVersionUID = -7097339472590357742L;
    private Map<InitialTrustScoreType, InitialTrustScoreData> initialTrustTypeToInitialTrustScoreDataMap;

    public BucketInitialTrustScoreEventsData() {
        super.setEventType(EventType.INITIAL_EVENT);
        initialTrustTypeToInitialTrustScoreDataMap = new ConcurrentHashMap<>();
    }
}
