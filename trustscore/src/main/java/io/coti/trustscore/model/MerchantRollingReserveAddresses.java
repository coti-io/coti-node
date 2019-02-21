package io.coti.trustscore.model;

import io.coti.basenode.data.MerchantRollingReserveAddressData;
import io.coti.basenode.model.Collection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class MerchantRollingReserveAddresses extends Collection<MerchantRollingReserveAddressData> {

    @PostConstruct
    public void init() {
        super.init();
    }
}
