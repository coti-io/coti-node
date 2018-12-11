package io.coti.financialserver.model;

import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.RollingReserveAddressData;

@Service
public class RollingReserveAddresses extends Collection<RollingReserveAddressData> {

    public RollingReserveAddresses() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
