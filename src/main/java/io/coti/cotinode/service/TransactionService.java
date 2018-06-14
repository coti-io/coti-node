package io.coti.cotinode.service;

import io.coti.cotinode.model.Transaction;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.service.interfaces.ITransactionService;
import io.coti.cotinode.storage.Interfaces.IPersistenceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransactionService implements ITransactionService {

    @Autowired
    IPersistenceProvider dbProvider;
    @Autowired
    UserHashValidationService userHashValidationService;
    @Autowired
    BalanceService balanceService;
    @Autowired
    ClusterService clusterService;
    @Autowired IPersistenceProvider persistenceProvider;

    @Override
    public boolean addNewTransaction(TransactionData transactionData) {

        if(!userHashValidationService.isLegalHash(transactionData.hash.getBytes())){
            return false;
        }

        if(!balanceService.isLegalTransaction(transactionData.hash.getBytes())){
            return false;
        }

        if(!balanceService.isLegalTransaction(transactionData.hash.getBytes())){
            return false;
        }

        balanceService.addToPreBalance(transactionData);

        transactionData = clusterService.addToCluster(transactionData);
        if(!transactionData.isAttached){
            balanceService.revertTransaction(transactionData);
            return false;
        }

        // Propogate??

        log.info(transactionData.toString());
        persistenceProvider.put(new Transaction(transactionData.hash.getBytes()));

        return true;
    }

    @Override
    public void cancelTransaction(TransactionData transactionData){

    }

    @Override
    public void confirmTransaction(TransactionData transactionData) {

    }
}