package io.coti.zerospend.services;

import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.data.TransactionIndexData;
import io.coti.common.model.TransactionIndexes;
import io.coti.zerospend.services.interfaces.ITransactionIndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class TransactionIndexService implements ITransactionIndexService {

    @Autowired
    private TransactionIndexes transactionIndexes;

    private Map<Hash, BigInteger> transactionsIndexMap;

    @PostConstruct
    private void init() {
        transactionsIndexMap = new ConcurrentHashMap<>();
    }

    @Override
    public synchronized void generateAndSetTransactionIndex(TransactionData transactionData) {
        BigInteger transactionNextIndex = BigInteger.valueOf(transactionsIndexMap.size());
        transactionsIndexMap.put(transactionData.getHash(), transactionNextIndex);
        transactionData.setIndex(transactionNextIndex);
        TransactionIndexData transactionIndexData = new TransactionIndexData(transactionData.getHash(), transactionNextIndex);
        transactionIndexes.put(transactionIndexData);
    }

    public Map<Hash, BigInteger> getTransactionsIndexMap() {
        return transactionsIndexMap;
    }

}
