package io.coti.trustscore.model;

import io.coti.basenode.model.Collection;
import io.coti.trustscore.data.scorebuckets.BucketData;
import org.springframework.stereotype.Service;

@Service
public class Buckets<T extends BucketData> extends Collection<T> {

    public void init() {
        super.init();

    }
}
