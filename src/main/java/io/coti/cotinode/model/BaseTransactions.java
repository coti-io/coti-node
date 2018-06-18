package io.coti.cotinode.model;

import io.coti.cotinode.data.BaseTransactionData;

import javax.annotation.PostConstruct;

public class BaseTransactions extends Collection<BaseTransactionData> {

    public BaseTransactions() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
