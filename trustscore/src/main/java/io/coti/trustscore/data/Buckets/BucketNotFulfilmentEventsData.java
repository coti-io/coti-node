package io.coti.trustscore.data.Buckets;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Events.NotFulfilmentEventsData;
import io.coti.trustscore.data.Events.NotFulfilmentToClientContributionData;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class BucketNotFulfilmentEventsData extends BucketEventData<NotFulfilmentEventsData> implements IEntity {

    private Map<Hash, NotFulfilmentToClientContributionData> clientHashToNotFulfilmentContributionMap;

    public BucketNotFulfilmentEventsData() {
        super.setEventType(EventType.NOT_FULFILMENT_EVENT);
        clientHashToNotFulfilmentContributionMap = new ConcurrentHashMap<>();
    }

}