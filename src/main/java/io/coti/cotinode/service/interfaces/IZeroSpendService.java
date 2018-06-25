package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.TransactionData;

public interface IZeroSpendService {
    TransactionData getZeroSpendTransaction(double trustScore);
}
