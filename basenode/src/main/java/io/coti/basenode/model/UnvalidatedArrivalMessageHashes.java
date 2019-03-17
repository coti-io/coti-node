package io.coti.basenode.model;

import io.coti.basenode.data.MessageArrivalValidationData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class UnvalidatedArrivalMessageHashes extends Collection<MessageArrivalValidationData> {

    public UnvalidatedArrivalMessageHashes() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }

}
