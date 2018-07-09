package io.coti.common.model;

import io.coti.common.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class Transactions extends Collection<TransactionData> {

    public Transactions() {
    }

    @PostConstruct
    public void init() {
        super.init();
    }
}
