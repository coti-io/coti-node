package io.coti.trustscore.model;

import io.coti.basenode.model.Collection;
import io.coti.trustscore.data.buckets.BucketEventData;
import org.springframework.stereotype.Service;

@Service
public class BucketEvents<T extends BucketEventData> extends Collection<T> {

}
