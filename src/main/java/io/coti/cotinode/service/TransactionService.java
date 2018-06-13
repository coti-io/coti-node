package io.coti.cotinode.service;

import io.coti.cotinode.model.TransactionPackage;
import io.coti.cotinode.service.interfaces.ITransactionService;
import io.coti.cotinode.storage.Interfaces.IPersistenceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionService implements ITransactionService {

    @Autowired
    IPersistenceProvider dbProvider;
    @Autowired
    UserHashValidationService userHashValidationService;
    @Autowired
    BalanceService balanceService;
    @Autowired
    BalanceService preBalanceService;
    @Autowired
    ClusterService clusterService;

    @Override
    public boolean addNewTransaction(TransactionPackage transactionPackage) {

        if(!userHashValidationService.isLegalHash(transactionPackage.hash)){
            return false;
        }

        if(!balanceService.isLegalTransaction(transactionPackage.hash)){
            return false;
        }

        if(!preBalanceService.isLegalTransaction(transactionPackage.hash)){
            return false;
        }

        preBalanceService.addToPreBalance(transactionPackage);

        if(!clusterService.addToCluster(transactionPackage)){
            preBalanceService.revertTransaction(transactionPackage);
            return false;
        }

        System.out.println(transactionPackage);

        return true;
    }

    @Override
    public void cancelTransaction(TransactionPackage transactionPackage){

    }

    @Override
    public void confirmTransaction(TransactionPackage transactionPackage) {

    }
}
