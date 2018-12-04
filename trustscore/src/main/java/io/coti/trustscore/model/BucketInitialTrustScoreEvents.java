package io.coti.trustscore.model;

import io.coti.basenode.model.Collection;
import io.coti.trustscore.data.Buckets.BucketInitialTrustScoreEventsData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class BucketInitialTrustScoreEvents extends Collection<BucketInitialTrustScoreEventsData> {

    @PostConstruct
    public void init() {
        super.init();
    }
}
