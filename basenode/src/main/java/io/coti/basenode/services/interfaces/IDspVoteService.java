package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.DspVote;
import io.coti.basenode.data.Hash;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

public interface IDspVoteService {

    void init();

    void handleVoteConclusion(DspConsensusResult dspConsensusResult);

    ConcurrentMap<Hash, List<DspVote>> getTransactionHashToVotesListMapping();
}
