package io.coti.zerospend.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeConfirmationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfirmationService extends BaseNodeConfirmationService {

    @Autowired
    private IndexService indexService;

    @Override
    protected boolean insertNewTransactionIndex(TransactionData transactionData) {
        return true;
    }

    @Override
    protected long incrementAndGetTotalConfirmed() {
        long totalConfirmed = super.incrementAndGetTotalConfirmed();
        indexService.createAndSendClusterStampPreparationMsg(totalConfirmed);
        return totalConfirmed;
    }

}
