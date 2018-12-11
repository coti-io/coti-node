package io.coti.financialserver.model;

import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.ConsumerDisputesData;

@Service
public class ConsumerDisputes extends Collection<ConsumerDisputesData> {

    public ConsumerDisputes() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
