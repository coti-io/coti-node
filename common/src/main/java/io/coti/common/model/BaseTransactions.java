package io.coti.common.model;

import io.coti.common.data.BaseTransactionData;

import javax.annotation.PostConstruct;

public class BaseTransactions extends Collection<BaseTransactionData> {

    public BaseTransactions() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
