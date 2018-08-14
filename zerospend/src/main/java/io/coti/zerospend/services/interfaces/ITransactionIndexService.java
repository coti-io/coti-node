package io.coti.zerospend.services.interfaces;

import io.coti.common.data.TransactionData;

import java.math.BigInteger;

public interface ITransactionIndexService {
    void generateAndSetTransactionIndex(TransactionData transactionData);
}
