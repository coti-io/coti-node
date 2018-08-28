package io.coti.common.services;

import io.coti.common.communication.interfaces.IPropagationPublisher;
import io.coti.common.data.DspConsensusResult;
import io.coti.common.services.interfaces.IBalanceService;
import io.coti.common.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class BaseNodeDspVoteService {
    @Autowired
    protected ITransactionHelper transactionHelper;
    @Autowired
    protected IBalanceService balanceService;
    @Autowired
    protected IPropagationPublisher propagationPublisher;

    public void handleVoteConclusion(DspConsensusResult dspConsensusResult) {
        log.debug("Received DspConsensus result for transaction: {}", dspConsensusResult.getHash());
        if (!transactionHelper.handleVoteConclusionResult(dspConsensusResult)) {
            log.error("Illegal Dsp consensus result for transaction: {}", dspConsensusResult.getHash());
        } else {
            balanceService.setDspcToTrue(dspConsensusResult);
            continueHandleVoteConclusion(dspConsensusResult);
        }
    }

    public abstract void continueHandleVoteConclusion(DspConsensusResult dspConsensusResult);
}
