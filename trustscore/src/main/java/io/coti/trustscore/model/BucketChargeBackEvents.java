package io.coti.trustscore.model;

import io.coti.basenode.model.Collection;
import io.coti.trustscore.data.Buckets.BucketChargeBackEventsData;
import org.springframework.stereotype.Service;

@Service
public class BucketChargeBackEvents extends Collection<BucketChargeBackEventsData> {

    public void init() {
        super.init();
    }
}