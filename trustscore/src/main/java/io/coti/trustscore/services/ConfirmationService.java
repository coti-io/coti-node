package io.coti.trustscore.services;


import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.services.BaseNodeConfirmationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.EnumSet;

@Primary
@Service
public class ConfirmationService extends BaseNodeConfirmationService {

    @Autowired
    private TrustScoreService trustScoreService;

    @Override
    protected void continueHandleDSPConfirmedTransaction(TransactionData transactionData) {
        super.continueHandleDSPConfirmedTransaction(transactionData);
        if (!EnumSet.of(TransactionType.ZeroSpend, TransactionType.Initial).contains(transactionData.getType())) {
            trustScoreService.addTransactionToTsCalculation(transactionData);
        }
    }
}
