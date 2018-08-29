package io.coti.trustscore.services;

import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.services.BaseNodeDspVoteService;
import org.springframework.stereotype.Service;

@Service
public class DspVoteService extends BaseNodeDspVoteService {
    @Override
    public void continueHandleVoteConclusion(DspConsensusResult dspConsensusResult) {
    }
}