package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.RollingReserveReleaseDateData;
import org.springframework.stereotype.Service;

@Service
public class RollingReserveReleaseDates extends Collection<RollingReserveReleaseDateData> {

    public void init() {
        super.init();
    }
}
