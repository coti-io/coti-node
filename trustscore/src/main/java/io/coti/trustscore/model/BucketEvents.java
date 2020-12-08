package io.coti.trustscore.model;

import io.coti.basenode.model.Collection;
import io.coti.trustscore.data.Buckets.BucketEventData;
import io.coti.trustscore.data.Events.EventData;
import org.springframework.stereotype.Service;

@Service
public class BucketEvents<T extends BucketEventData<? extends EventData>> extends Collection<T> {

}
