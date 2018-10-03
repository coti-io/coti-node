package io.coti.trustscore.services;

import io.coti.basenode.data.TransactionData;

import io.coti.basenode.services.BaseNodeTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransactionService extends BaseNodeTransactionService{

        @Autowired
        TrustScoreService trustScoreService;


        @Override
        protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
                if (transactionData.getDspConsensusResult() != null && transactionData.getDspConsensusResult().isDspConsensus())
                        trustScoreService.addTransactionToTsCalculation(transactionData);
        }

}
