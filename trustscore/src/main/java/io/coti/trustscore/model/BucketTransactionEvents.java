package io.coti.trustscore.model;

import io.coti.basenode.model.Collection;
import io.coti.trustscore.data.Buckets.BucketTransactionEventsData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class BucketTransactionEvents extends Collection<BucketTransactionEventsData> {

    public BucketTransactionEvents() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}