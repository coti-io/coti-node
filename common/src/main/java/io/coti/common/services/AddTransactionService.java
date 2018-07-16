package io.coti.common.services;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.data.TransactionIndexData;
import io.coti.common.http.AddTransactionZeroSpendRequest;
import io.coti.common.http.AddTransactionZeroSpendResponse;
import io.coti.common.model.TransactionIndex;
import io.coti.common.services.interfaces.IAddTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AddTransactionService implements IAddTransactionService {

    @Autowired
    private TransactionIndex transactionIndex;

    private Map<Hash, Long> transactionDataMap;

    @PostConstruct
    public void init() {
        transactionDataMap = new ConcurrentHashMap<>();
    }

    @Override
    public ResponseEntity<AddTransactionZeroSpendResponse> addTransaction(AddTransactionZeroSpendRequest addTransactionRequest) {
        AddTransactionZeroSpendResponse addTransactionResponse = new AddTransactionZeroSpendResponse();
        TransactionData transactionData = addTransactionRequest.getTransactionData();
        long transactionNextIndex = transactionDataMap.size();
        transactionDataMap.put(addTransactionRequest.getTransactionData().getKey(), transactionNextIndex);
        transactionData.setIndex(transactionNextIndex);
        TransactionIndexData transactionIndexData = new TransactionIndexData(transactionData.getKey(), transactionNextIndex);
        transactionIndex.put(transactionIndexData);
        addTransactionResponse.setTransactionData(transactionData);
        return ResponseEntity.status(HttpStatus.OK).body(addTransactionResponse);
    }


    public TransactionData addTransaction2(AddTransactionZeroSpendRequest addTransactionRequest) {
        TransactionData transactionData = addTransactionRequest.getTransactionData();
        long transactionNextIndex = transactionDataMap.size();
        transactionDataMap.put(addTransactionRequest.getTransactionData().getKey(), transactionNextIndex);
        transactionData.setIndex(transactionNextIndex);
        TransactionIndexData transactionIndexData = new TransactionIndexData(transactionData.getKey(), transactionNextIndex);
        transactionIndex.put(transactionIndexData);
        return transactionData;
    }

    public Map<Hash, Long> getTransactionDataMap() {
        return transactionDataMap;
    }
}
