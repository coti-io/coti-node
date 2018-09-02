package io.coti.basenode.services.interfaces;


import io.coti.basenode.data.TransactionData;

import java.util.List;

public interface IZeroSpendService {

    List<TransactionData> getGenesisTransactions();
    TransactionData getZeroSpendTransaction(double trustScore);
}
