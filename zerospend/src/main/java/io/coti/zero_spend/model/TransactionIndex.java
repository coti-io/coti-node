package io.coti.zero_spend.model;

import io.coti.common.model.Collection;
import io.coti.zero_spend.data.TransactionIndexData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class TransactionIndex extends Collection<TransactionIndexData> {

    public TransactionIndex() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }

}
