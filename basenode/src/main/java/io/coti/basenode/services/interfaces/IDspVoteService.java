package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.DspConsensusResult;

public interface IDspVoteService {

    void init();

    void handleVoteConclusion(DspConsensusResult dspConsensusResult);
}
