package io.coti.nodemanager.services;

import io.coti.basenode.http.DeleteRejectedTransactionsRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeTransactionService;
import org.springframework.http.ResponseEntity;

public class TransactionService extends BaseNodeTransactionService {

    @Override
    public ResponseEntity<IResponse> getRejectedTransactions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseEntity<IResponse> deleteRejectedTransactions(DeleteRejectedTransactionsRequest deleteRejectedTransactionsRequest) {
        throw new UnsupportedOperationException();
    }
}
