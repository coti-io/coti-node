package io.coti.trustscore.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeTransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransactionService extends BaseNodeTransactionService {

    @Override
    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
    }
}
