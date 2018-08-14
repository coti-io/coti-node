package io.coti.zerospend.services.interfaces;

import io.coti.common.data.TransactionData;

public interface IZeroSpendTrxService {
    void receiveZeroSpendTransaction(TransactionData transactionDataFromDSP);
}
