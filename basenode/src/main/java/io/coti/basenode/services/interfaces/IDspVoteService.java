package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.Hash;

public interface IDspVoteService {

    void init();

    void handleVoteConclusion(DspConsensusResult dspConsensusResult);

    DspConsensusResult getPostponedDspConsensusResult(Hash transactionHash);

}
