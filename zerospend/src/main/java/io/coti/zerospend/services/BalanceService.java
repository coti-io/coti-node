package io.coti.zerospend.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeBalanceService;
import org.springframework.stereotype.Service;

@Service
public class BalanceService extends BaseNodeBalanceService {

    @Override
    protected void insertNewTransactionIndex(TransactionData transactionData) {
    }
}
