package io.coti.cotinode.service;

import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.service.interfaces.ITransactionService;
import io.coti.cotinode.storage.Interfaces.IDatabaseConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransactionService implements ITransactionService {
    @Autowired
    private UserHashValidationService userHashValidationService;
    @Autowired
    private BalanceService balanceService;
    @Autowired
    private ClusterService clusterService;

    public boolean addNewTransaction(TransactionData transactionData) {

        if (!userHashValidationService.isLegalHash(transactionData.getHash())) {
            return false;
        }

        if (!balanceService.isLegalTransaction(transactionData.getHash())) {
            return false;
        }

        if (!balanceService.isLegalTransaction(transactionData.getHash())) {
            return false;
        }

        balanceService.addToPreBalance(transactionData);

        transactionData = clusterService.addToCluster(transactionData);

        log.info(transactionData.toString());

        return true;
    }
}