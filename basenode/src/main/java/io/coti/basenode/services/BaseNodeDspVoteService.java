package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.services.interfaces.IConfirmationService;
import io.coti.basenode.services.interfaces.IDspVoteService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BaseNodeDspVoteService implements IDspVoteService {
    @Autowired
    protected ITransactionHelper transactionHelper;
    @Autowired
    protected IConfirmationService confirmationService;
    @Autowired
    protected IPropagationPublisher propagationPublisher;

    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    public void handleVoteConclusion(DspConsensusResult dspConsensusResult) {
        log.debug("Received DspConsensus result for transaction: {}", dspConsensusResult.getHash());
        if (transactionHelper.handleVoteConclusionResult(dspConsensusResult)) {
            confirmationService.setDspcToTrue(dspConsensusResult);
            continueHandleVoteConclusion(dspConsensusResult);
        }
    }

    protected void continueHandleVoteConclusion(DspConsensusResult dspConsensusResult) {
        log.debug("Continue to handle vote conclusion by base node");
    }
}
