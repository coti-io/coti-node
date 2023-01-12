package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionDspVote;

public interface IDspVoteService {

    void init();

    void handleVoteConclusion(DspConsensusResult dspConsensusResult);

    DspConsensusResult getPostponedDspConsensusResult(Hash transactionHash);

    void setIndexForDspResult(TransactionData transactionData, DspConsensusResult dspConsensusResult);

    void publishDecision(Hash hash);

    void preparePropagatedTransactionForVoting(TransactionData transactionData);

    void receiveDspVote(TransactionDspVote data);
}
