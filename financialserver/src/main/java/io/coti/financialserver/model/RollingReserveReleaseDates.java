package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.RollingReserveReleaseDateData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class RollingReserveReleaseDates extends Collection<RollingReserveReleaseDateData> {

    public RollingReserveReleaseDates() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
