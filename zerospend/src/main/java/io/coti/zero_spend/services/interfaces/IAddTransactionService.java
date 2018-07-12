package io.coti.zero_spend.services.interfaces;

import io.coti.zero_spend.http.AddTransactionRequest;

public interface IAddTransactionService {

    void addTransaction(AddTransactionRequest addTransactionRequest);

}
