package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.ConsumerDisputesData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class ConsumerDisputes extends Collection<ConsumerDisputesData> {

    public ConsumerDisputes() {

    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
