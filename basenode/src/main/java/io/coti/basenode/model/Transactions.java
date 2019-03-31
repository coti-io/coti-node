package io.coti.basenode.model;

import io.coti.basenode.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Transactions extends Collection<TransactionData> {

    public Transactions() {
    }

    public void init() {
        super.init();
    }
}
