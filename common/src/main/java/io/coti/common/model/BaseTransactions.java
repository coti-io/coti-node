package io.coti.common.model;

import io.coti.common.data.BaseTransactionData;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class BaseTransactions extends Collection<BaseTransactionData> {

    public BaseTransactions() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
