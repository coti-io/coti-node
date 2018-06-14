package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.TransactionData;

public interface ITransactionService {

    boolean addNewTransaction(TransactionData transactionData);

    void cancelTransaction(TransactionData transactionData);

    void confirmTransaction(TransactionData transactionData);


}
