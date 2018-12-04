package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.RollingReserveAddressData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class RollingReserveAddresses extends Collection<RollingReserveAddressData> {

    public RollingReserveAddresses() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
