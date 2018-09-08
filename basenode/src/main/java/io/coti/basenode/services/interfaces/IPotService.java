package io.coti.basenode.services.interfaces;


import io.coti.basenode.data.TransactionData;

public interface IPotService {

    void init();

    boolean validatePot(TransactionData transactionData);
}
