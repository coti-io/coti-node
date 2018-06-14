package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.TransactionData;

public interface ITransactionService {

    boolean addNewTransaction(TransactionData transactionData);
}
