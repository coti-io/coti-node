package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.MerchantRollingReserveData;
import org.springframework.stereotype.Service;

@Service
public class MerchantRollingReserves extends Collection<MerchantRollingReserveData> {

    public void init() {
        super.init();
    }
}
