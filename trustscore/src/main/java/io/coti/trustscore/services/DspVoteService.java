package io.coti.trustscore.services;

import io.coti.common.data.DspConsensusResult;
import io.coti.common.services.BaseNodeDspVoteService;
import org.springframework.stereotype.Service;

@Service
public class DspVoteService extends BaseNodeDspVoteService {
    @Override
    public void continueHandleVoteConclusion(DspConsensusResult dspConsensusResult) {
    }
}