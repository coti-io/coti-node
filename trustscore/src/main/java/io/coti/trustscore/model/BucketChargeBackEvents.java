package io.coti.trustscore.model;

import io.coti.basenode.model.Collection;
import io.coti.trustscore.data.Buckets.BucketChargeBackEventsData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class BucketChargeBackEvents extends Collection<BucketChargeBackEventsData> {

    @PostConstruct
    public void init() {
        super.init();
    }
}