package io.coti.trustscore.model;

import io.coti.basenode.model.Collection;
import io.coti.trustscore.data.TransactionEventData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class TransactionEvents extends Collection<TransactionEventData> {

    public TransactionEvents() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
