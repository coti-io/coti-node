package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.UserDisputesData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class ArbitratorDisputes extends Collection<UserDisputesData> {

    public ArbitratorDisputes() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
