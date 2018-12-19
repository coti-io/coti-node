package io.coti.financialserver.model;

import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.RollingReserveData;

@Service
public class RollingReserves extends Collection<RollingReserveData> {

    public RollingReserves() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
