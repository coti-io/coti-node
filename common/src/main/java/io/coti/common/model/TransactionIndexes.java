package io.coti.common.model;

import io.coti.common.data.TransactionIndexData;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class TransactionIndexes extends Collection<TransactionIndexData> {

    public TransactionIndexes() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}