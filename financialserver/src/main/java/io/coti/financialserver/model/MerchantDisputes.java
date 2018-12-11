package io.coti.financialserver.model;

import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.MerchantDisputesData;

@Service
public class MerchantDisputes extends Collection<MerchantDisputesData> {

    public MerchantDisputes() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
