package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.RollingReserveData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class RollingReserves extends Collection<RollingReserveData> {

    public RollingReserves() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
