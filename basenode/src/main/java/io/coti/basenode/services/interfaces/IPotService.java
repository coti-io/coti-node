package io.coti.basenode.services.interfaces;


import io.coti.basenode.data.TransactionData;

public interface IPotService {
    boolean validatePot(TransactionData transactionData);
}
