package io.coti.cotinode.service;

import io.coti.cotinode.model.Transaction;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    public Transaction getTransaction(byte[] hash) {
        return new Transaction(hash);
    }
}
