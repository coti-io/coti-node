package io.coti.zerospend.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeConfirmationService;
import org.springframework.stereotype.Service;

@Service
public class ConfirmationService extends BaseNodeConfirmationService {

    @Override
    protected boolean insertNewTransactionIndex(TransactionData transactionData) {
        return true;
    }
}
