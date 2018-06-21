package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;

import java.util.List;
import java.util.Map;

public interface ITransactionService {

    boolean addNewTransaction(List<Map.Entry<Hash, Double>> transferredAmounts);

    TransactionData getTransactionData(Hash transactionHash);
}
