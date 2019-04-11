package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.DistributionData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class Distributions extends Collection<DistributionData> {

    public Distributions() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
