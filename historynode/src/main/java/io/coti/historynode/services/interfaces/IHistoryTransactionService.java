package io.coti.historynode.services.interfaces;

import io.coti.basenode.http.interfaces.IResponse;
import io.coti.historynode.http.GetTransactionsRequestOld;
import org.springframework.http.ResponseEntity;

public interface IHistoryTransactionService {

    ResponseEntity<IResponse> getTransactionsDetails(GetTransactionsRequestOld getTransactionRequest);

    void deleteLocalUnconfirmedTransactions();
}
