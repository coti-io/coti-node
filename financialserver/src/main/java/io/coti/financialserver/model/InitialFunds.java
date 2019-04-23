package io.coti.financialserver.model;

import io.coti.basenode.data.InitialFundData;
import io.coti.basenode.model.Collection;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class InitialFunds extends Collection<InitialFundData> {

    public InitialFunds() {
    }

    @Override
    public void init() {
        super.init();
    }
}
