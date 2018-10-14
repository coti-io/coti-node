package io.coti.trustscore.model;

import io.coti.basenode.model.Collection;
import io.coti.trustscore.data.HistoryBucketEvents;

import javax.annotation.PostConstruct;

public class HistoryBuckets extends Collection<HistoryBucketEvents> {

    @PostConstruct
    public void init() {
        super.init();
    }
}
