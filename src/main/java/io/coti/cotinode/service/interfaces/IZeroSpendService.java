package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.TransactionData;

import java.util.ArrayList;
import java.util.List;

public interface IZeroSpendService {
    TransactionData getZeroSpendTransaction(double trustScore);

    List<TransactionData> getGenesisTransactions();
}
