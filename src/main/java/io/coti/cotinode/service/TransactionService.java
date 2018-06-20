package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.model.Transactions;
import io.coti.cotinode.service.interfaces.IValidationService;
import io.coti.cotinode.service.interfaces.ITransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TransactionService implements ITransactionService {
    @Autowired
    private UserHashValidationService userHashValidationService;
    @Autowired
    private BalanceService balanceService;
    @Autowired
    private ClusterService clusterService;
    @Autowired
    private Transactions transactions;
    @Autowired
    private IValidationService validationService;

    @Override
    public boolean addNewTransaction(List<Map.Entry<Hash, Double>> transferredAmounts) {
//        if(!validateDataIntegrity(transactionData)){
//            return false;
//        }
//
//        if(!sourceValidationService.validateSources(transactionData)){
//            return false;
//        }
//
//        balanceService.addToPreBalance(transactionData);
//        transactionData = clusterService.addToCluster(transactionData);
//        transactions.put(transactionData);
        return true;
    }

    private boolean validateDataIntegrity(TransactionData transactionData) {
        if (!userHashValidationService.isLegalHash(transactionData.getHash())) {
            return false;
        }
//        if (!balanceService.isLegalTransaction(transactionData.getHash())) {
//            return false;
//        }
//        if (!balanceService.isLegalTransaction(transactionData.getHash())) {
//            return false;
//        }
        return true;
    }

    @Override
    public TransactionData getTransactionData(Hash transactionHash) {
        if(transactionHash.toString().equals("NULL")){
            throw new NullPointerException();
        }
        if(transactionHash.toString().equals("N")){
            throw new IllegalThreadStateException();
        }
        return transactions.getByHash(transactionHash);
    }

}