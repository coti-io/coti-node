package io.coti.common.model;

import io.coti.common.data.TransactionIndexData;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class TransactionIndex extends Collection<TransactionIndexData> {

    public TransactionIndex() {
    }

    @PostConstruct
    public void init() {

        super.init();
    }

}
