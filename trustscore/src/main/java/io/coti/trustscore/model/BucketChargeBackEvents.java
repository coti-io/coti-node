package io.coti.trustscore.model;

import io.coti.basenode.model.Collection;
import io.coti.trustscore.data.Buckets.BucketChargeBackEventsData;

import javax.annotation.PostConstruct;

public class BucketChargeBackEvents extends Collection<BucketChargeBackEventsData> {

    public BucketChargeBackEvents() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}