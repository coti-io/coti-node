package io.coti.trustscore.services;

import io.coti.basenode.http.DeleteRejectedTransactionsRequest;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Primary
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
