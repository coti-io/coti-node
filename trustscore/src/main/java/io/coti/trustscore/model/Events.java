package io.coti.trustscore.model;

import io.coti.basenode.model.Collection;
import io.coti.trustscore.data.EventData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class Events extends Collection<EventData> {

    public Events() {
    }

    @PostConstruct
    public void init() {
        super.init();

    }
}
