package io.coti.historynode.services.interfaces;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.historynode.http.GetTransactionsRequest;
import org.springframework.http.ResponseEntity;

public interface ITransactionService {

    ResponseEntity<IResponse> getTransactionsDetails(GetTransactionsRequest getTransactionRequest);

    void deleteLocalUnconfirmedTransactions();
}
