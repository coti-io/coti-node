package io.coti.trustscore.services;

import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeDspVoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DspVoteService extends BaseNodeDspVoteService {

    @Autowired
    private TrustScoreService trustScoreService;
    @Autowired
    private Transactions transactions;

    @Override
    protected void continueHandleVoteConclusion(DspConsensusResult dspConsensusResult) {

        TransactionData transactionData = transactions.getByHash(dspConsensusResult.getHash());

        trustScoreService.addTransactionToTsCalculation(transactionData);
    }

}
