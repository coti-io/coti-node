package io.coti.zero_spend.services;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.zero_spend.http.AddTransactionRequest;
import io.coti.zero_spend.services.interfaces.IAddTransactionService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AddTransactionService implements IAddTransactionService {


    private Map<Hash, TransactionData> transactionDataMap;

    @PostConstruct
    public void init() {
        transactionDataMap = new ConcurrentHashMap<>();
    }

    @Override
    public void addTransaction(AddTransactionRequest addTransactionRequest) {
        transactionDataMap.put(addTransactionRequest.getTransactionData().getKey(), addTransactionRequest.getTransactionData());

    }

    public Map<Hash, TransactionData> getTransactionDataMap() {
        return transactionDataMap;
    }
}
