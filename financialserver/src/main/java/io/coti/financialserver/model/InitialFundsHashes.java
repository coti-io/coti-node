package io.coti.financialserver.model;

import io.coti.basenode.data.InitialFundDataHash;
import io.coti.basenode.model.Collection;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class InitialFundsHashes extends Collection<InitialFundDataHash> {

    public InitialFundsHashes() {
    }

    @Override
    @PostConstruct
    public void init() {
        super.init();
    }
}
