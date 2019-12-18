package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.RollingReserveReleaseDateData;
import org.springframework.stereotype.Service;

import java.io.Serializable;

@Service
public class RollingReserveReleaseDates extends Collection<RollingReserveReleaseDateData>  {

    @Override
    public void init() {
        super.init();
    }
}
