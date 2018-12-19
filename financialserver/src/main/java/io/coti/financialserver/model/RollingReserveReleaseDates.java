package io.coti.financialserver.model;

import javax.annotation.PostConstruct;

import io.coti.financialserver.data.RollingReserveReleaseDateData;
import org.springframework.stereotype.Service;

import io.coti.basenode.model.Collection;

@Service
public class RollingReserveReleaseDates extends Collection<RollingReserveReleaseDateData> {

    public RollingReserveReleaseDates() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
