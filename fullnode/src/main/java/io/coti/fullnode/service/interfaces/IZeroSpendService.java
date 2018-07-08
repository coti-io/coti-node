package io.coti.fullnode.service.interfaces;


import io.coti.common.data.TransactionData;

import java.util.List;

public interface IZeroSpendService {
    TransactionData getZeroSpendTransaction(double trustScore);

    List<TransactionData> getGenesisTransactions();
}
