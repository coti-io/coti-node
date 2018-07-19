package io.coti.dspnode.services;

import io.coti.common.data.TransactionData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

@Slf4j
@Service
public class UnconfirmedTransactionsReceiver {
    @Autowired
    private TransactionReceiver transactionReceiver;
    @Autowired
    private TransactionService transactionService;
    Consumer<TransactionData> transactionDataHandler = transactionData -> {
        transactionService.attachUnconfirmedPropagatedTransaction(transactionData);
    };

    @PostConstruct
    private void init() {
        transactionReceiver.init(transactionDataHandler);
    }
}

