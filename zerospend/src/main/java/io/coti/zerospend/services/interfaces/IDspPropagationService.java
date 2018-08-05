package io.coti.zerospend.services.interfaces;

import io.coti.common.data.TransactionData;

public interface IDspPropagationService {
    void acceptPropagation(TransactionData transactionData);
}
