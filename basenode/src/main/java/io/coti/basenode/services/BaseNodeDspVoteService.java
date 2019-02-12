package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.DspVote;
import io.coti.basenode.data.Hash;
import io.coti.basenode.services.interfaces.IConfirmationService;
import io.coti.basenode.services.interfaces.IDspVoteService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
public class BaseNodeDspVoteService implements IDspVoteService {
    @Autowired
    protected ITransactionHelper transactionHelper;
    @Autowired
    protected IConfirmationService confirmationService;
    @Autowired
    protected IPropagationPublisher propagationPublisher;

    protected ConcurrentMap<Hash, List<DspVote>> transactionHashToVotesListMapping;

    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
        transactionHashToVotesListMapping = new ConcurrentHashMap<>();
    }

    public void handleVoteConclusion(DspConsensusResult dspConsensusResult) {
        log.debug("Received DspConsensus result for transaction: {}", dspConsensusResult.getHash());
        if (!transactionHelper.handleVoteConclusionResult(dspConsensusResult)) {
            log.error("Illegal Dsp consensus result for transaction: {}", dspConsensusResult.getHash());
        } else {
            confirmationService.setDspcToTrue(dspConsensusResult);
            continueHandleVoteConclusion(dspConsensusResult);
        }
    }

    public ConcurrentMap<Hash, List<DspVote>> getTransactionHashToVotesListMapping() {
        return transactionHashToVotesListMapping;
    }

    protected void continueHandleVoteConclusion(DspConsensusResult dspConsensusResult) {
    }
}
