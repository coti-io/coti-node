package io.coti.dspnode.services;

import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.BaseNodeDspVoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
public class DspVoteService extends BaseNodeDspVoteService {

    @Override
    public void continueHandleVoteConclusion(DspConsensusResult dspConsensusResult) {
        propagationPublisher.propagate(dspConsensusResult, Collections.singletonList(
                NodeType.FullNode));
    }
}
