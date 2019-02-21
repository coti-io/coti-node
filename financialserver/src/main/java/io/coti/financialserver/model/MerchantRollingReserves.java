package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.MerchantRollingReserveData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class MerchantRollingReserves extends Collection<MerchantRollingReserveData> {

    public MerchantRollingReserves() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
