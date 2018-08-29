package io.coti.basenode.services.interfaces;


import io.coti.basenode.data.TransactionData;

import java.util.List;

public interface IZeroSpendService {
    TransactionData getZeroSpendTransaction(double trustScore);

    List<TransactionData> getGenesisTransactions();
}
