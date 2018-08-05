package io.coti.common.model;

import io.coti.common.data.NonIndexedTransactionsData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class NonIndexedTransactions extends Collection<NonIndexedTransactionsData> {
    public NonIndexedTransactions() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
