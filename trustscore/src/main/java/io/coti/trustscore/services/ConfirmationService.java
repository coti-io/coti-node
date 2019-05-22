package io.coti.trustscore.services;


import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.services.BaseNodeConfirmationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class ConfirmationService extends BaseNodeConfirmationService {

    @Autowired
    TrustScoreService trustScoreService;

    @Override
    protected void scoreDSPConfirmedTransaction(TransactionData transactionData) {
        if (!transactionData.getType().equals(TransactionType.ZeroSpend)) {
            trustScoreService.addTransactionToTsCalculation(transactionData);
        }
    }
}
