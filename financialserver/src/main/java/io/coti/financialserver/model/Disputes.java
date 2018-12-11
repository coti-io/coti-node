package io.coti.financialserver.model;

import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.DisputeData;

@Service
public class Disputes extends Collection<DisputeData> {

    public Disputes() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
