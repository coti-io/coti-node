package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.DisputeData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class Disputes extends Collection<DisputeData> {

    public Disputes() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
