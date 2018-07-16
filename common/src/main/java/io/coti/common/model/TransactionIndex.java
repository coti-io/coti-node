package io.coti.common.model;

import io.coti.common.data.TransactionIndexData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class TransactionIndex extends Collection<TransactionIndexData> {

    public TransactionIndex() {
    }

    @PostConstruct
    public void init() {

        super.init();
    }

}
