package io.coti.zerospend.services;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.data.TransactionIndexData;
import io.coti.common.model.TransactionIndexes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TransactionIndexService {

    @Autowired
    private TransactionIndexes transactionIndexes;
    private Map<Hash, Long> transactionsIndexMap;

    @PostConstruct
    private void init() {
        transactionsIndexMap = new ConcurrentHashMap<>();
    }

    public synchronized long generateTransactionIndex(TransactionData transactionData) {
        long transactionNextIndex = transactionsIndexMap.size();
        transactionsIndexMap.put(transactionData.getHash(), transactionNextIndex);
        TransactionIndexData transactionIndexData = new TransactionIndexData(transactionData.getHash(), transactionNextIndex);
        transactionIndexes.put(transactionIndexData);
        return transactionNextIndex;
    }
}