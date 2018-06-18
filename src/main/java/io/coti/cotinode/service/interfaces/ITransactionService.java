package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;

public interface ITransactionService {

    boolean addNewTransaction(TransactionData transactionData);

    TransactionData getTransactionData(Hash transactionHash);
}
