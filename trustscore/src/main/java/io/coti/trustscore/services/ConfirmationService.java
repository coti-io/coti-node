package io.coti.trustscore.services;


import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.services.BaseNodeConfirmationService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

import static io.coti.trustscore.services.NodeServiceManager.trustScoreService;

@Primary
@Service
public class ConfirmationService extends BaseNodeConfirmationService {

    @Override
    protected void continueHandleDSPConfirmedTransaction(TransactionData transactionData) {
        if (!EnumSet.of(TransactionType.ZeroSpend, TransactionType.Initial).contains(transactionData.getType())) {
            trustScoreService.addTransactionToTsCalculation(transactionData);
        }
    }
}
