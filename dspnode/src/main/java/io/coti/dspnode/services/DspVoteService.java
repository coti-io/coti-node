package io.coti.dspnode.services;

import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.BaseNodeDspVoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Collections;

import static io.coti.dspnode.services.NodeServiceManager.propagationPublisher;

@Slf4j
@Service
@Primary
public class DspVoteService extends BaseNodeDspVoteService {

    @Override
    public void continueHandleVoteConclusion(DspConsensusResult dspConsensusResult) {
        propagationPublisher.propagate(dspConsensusResult, Collections.singletonList(
                NodeType.FullNode));
    }
}
