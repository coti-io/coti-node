package io.coti.cotinode.service;

import io.coti.cotinode.model.Transaction;

public interface ITransactionService {

    public Transaction getTransaction(byte[] hash);
}
