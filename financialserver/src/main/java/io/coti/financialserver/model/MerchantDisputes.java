package io.coti.financialserver.model;

import io.coti.basenode.model.Collection;
import io.coti.financialserver.data.MerchantDisputesData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class MerchantDisputes extends Collection<MerchantDisputesData> {

    public MerchantDisputes() {

    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
