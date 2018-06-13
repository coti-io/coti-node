package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.model.TransactionPackage;

public interface ITransactionService {

    boolean addNewTransaction(TransactionPackage transactionPackage);

    void cancelTransaction(TransactionPackage transactionPackage);

    void confirmTransaction(TransactionPackage transactionPackage);


}
