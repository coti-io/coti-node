package io.coti.zerospend.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeConfirmationService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class ConfirmationService extends BaseNodeConfirmationService {

    @Override
    public boolean insertNewTransactionIndex(TransactionData transactionData) {
        return true;
    }
}
