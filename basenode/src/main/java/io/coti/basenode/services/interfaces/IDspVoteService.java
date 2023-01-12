package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.*;

public interface IDspVoteService {

    void init();

    void handleVoteConclusion(DspConsensusResult dspConsensusResult);

    DspConsensusResult getPostponedDspConsensusResult(Hash transactionHash);

    void handleDspConsensusResultResend(NodeResendDcrData nodeResendDcrData);

    void setIndexForDspResult(TransactionData transactionData, DspConsensusResult dspConsensusResult);

    void publishDecision(Hash hash);

    void preparePropagatedTransactionForVoting(TransactionData transactionData);

    void receiveDspVote(TransactionDspVote data);
}
