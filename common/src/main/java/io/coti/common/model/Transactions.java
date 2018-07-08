package io.coti.common.model;

import io.coti.common.data.TransactionData;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;

@Slf4j
public class Transactions extends Collection<TransactionData> {

    public Transactions() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
