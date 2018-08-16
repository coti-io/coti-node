package io.coti.common.services.interfaces;

import io.coti.common.data.TransactionData;

public interface IPoftService {

    void poftAction(TransactionData transactionData);

    boolean validatePoft(TransactionData transactionData);
}
