package io.coti.trustscore.data.buckets;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.interfaces.IEntity;
import io.coti.trustscore.data.enums.EventType;
import io.coti.trustscore.data.events.NotFulfilmentEventsData;
import io.coti.trustscore.data.events.NotFulfilmentToClientContributionData;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class BucketNotFulfilmentEventsData extends BucketEventData<NotFulfilmentEventsData> implements IEntity {

    private static final long serialVersionUID = 532162141160571858L;
    private Map<Hash, NotFulfilmentToClientContributionData> clientHashToNotFulfilmentContributionMap;

    public BucketNotFulfilmentEventsData() {
        super.setEventType(EventType.NOT_FULFILMENT_EVENT);
        clientHashToNotFulfilmentContributionMap = new ConcurrentHashMap<>();
    }

}