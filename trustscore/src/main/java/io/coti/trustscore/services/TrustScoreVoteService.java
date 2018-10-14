package io.coti.trustscore.services;


import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeDspVoteService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;


@Data
@Service
@Primary
public class TrustScoreVoteService extends BaseNodeDspVoteService {


    @Autowired
    TrustScoreService trustScoreService;

    @Autowired
    Transactions transactions;

    @Override
    protected void continueHandleVoteConclusion(DspConsensusResult dspConsensusResult) {


        TransactionData transactionData = transactions.getByHash(dspConsensusResult.getTransactionHash());
        transactionData.setDspConsensusResult(dspConsensusResult);
        trustScoreService.addTransactionToTsCalculation(transactionData);
    }
}
