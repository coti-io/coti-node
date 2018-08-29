package io.coti.basenode.model;

import io.coti.basenode.data.TransactionIndexData;
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