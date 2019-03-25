package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.DistributionReleaseDateData;
import io.coti.financialserver.data.RollingReserveReleaseDateData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class DistributionReleaseDates extends Collection<DistributionReleaseDateData> {

    public DistributionReleaseDates() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
