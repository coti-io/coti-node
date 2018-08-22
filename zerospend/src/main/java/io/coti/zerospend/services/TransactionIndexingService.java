package io.coti.zerospend.services;

import io.coti.common.data.TransactionData;
import io.coti.common.data.TransactionIndexData;
import io.coti.common.model.TransactionIndexes;
import io.coti.common.model.Transactions;
import io.coti.common.services.TransactionIndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransactionIndexingService {
    @Autowired
    private Transactions transactions;
    @Autowired
    private TransactionIndexes transactionIndexes;
    @Autowired
    private TransactionIndexService transactionIndexService;

    public synchronized long generateTransactionIndex(TransactionData transactionData) {
        TransactionIndexData lastTransactionIndexData = transactionIndexService.getLastTransactionIndex();
        TransactionData previousTransactionData = transactions.getByHash(lastTransactionIndexData.getTransactionHash());
        TransactionIndexData transactionIndexData =
                new TransactionIndexData(transactionData.getHash(),
                        lastTransactionIndexData.getIndex() + 1,
                        TransactionIndexService.getAccumulatedHash(lastTransactionIndexData.getAccumulatedHash(), previousTransactionData));
        transactionIndexes.put(transactionIndexData);
        log.info("Transaction Index set: {}", transactionIndexData.getIndex());
        return transactionIndexData.getIndex();
    }

}