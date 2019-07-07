package io.coti.basenode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.DspConsensusCrypto;
import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.exceptions.DspConsensusResultException;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IConfirmationService;
import io.coti.basenode.services.interfaces.IDspVoteService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class BaseNodeDspVoteService implements IDspVoteService {
    @Autowired
    protected ITransactionHelper transactionHelper;
    @Autowired
    protected IConfirmationService confirmationService;
    @Autowired
    protected IPropagationPublisher propagationPublisher;
    @Autowired
    private DspConsensusCrypto dspConsensusCrypto;
    @Autowired
    private Transactions transactions;
    private Map<Hash, DspConsensusResult> postponedDspConsensusResultsMap;

    public void init() {
        postponedDspConsensusResultsMap = new ConcurrentHashMap<>();
        log.info("{} is up", this.getClass().getSimpleName());
    }

    public synchronized void handleVoteConclusion(DspConsensusResult dspConsensusResult) {
        try {
            log.debug("Received DspConsensus result for transaction: {}", dspConsensusResult.getHash());
            handleVoteConclusionResult(dspConsensusResult);
            confirmationService.setDspcToTrue(dspConsensusResult);
            continueHandleVoteConclusion(dspConsensusResult);
            postponedDspConsensusResultsMap.remove(dspConsensusResult.getHash());
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
        }
    }

    private void handleVoteConclusionResult(DspConsensusResult dspConsensusResult) {
        if (!dspConsensusCrypto.verifySignature(dspConsensusResult)) {
            throw new DspConsensusResultException(String.format("DspConsensus signature verification failed for transaction %s", dspConsensusResult.getHash()));
        }
        TransactionData transactionData = transactions.getByHash(dspConsensusResult.getHash());
        if (transactionData == null) {
            postponedDspConsensusResultsMap.put(dspConsensusResult.getHash(), dspConsensusResult);
            throw new DspConsensusResultException(String.format("DspConsensus result is for a non-existing transaction %s. ", dspConsensusResult.getHash()));
        }
        if (transactionData.getDspConsensusResult() != null) {
            log.debug("DspConsensus result already exists for transaction {}", dspConsensusResult.getHash());
        }
        if (dspConsensusResult.isDspConsensus()) {
            log.debug("Valid vote conclusion received for transaction: {}", dspConsensusResult.getHash());
        } else {
            log.debug("Invalid vote conclusion received for transaction: {}", dspConsensusResult.getHash());
        }

        log.debug("DspConsensus result for transaction: Hash= {}, DspVoteResult= {}, Index= {}", dspConsensusResult.getHash(), dspConsensusResult.isDspConsensus(), dspConsensusResult.getIndex());
    }

    protected void continueHandleVoteConclusion(DspConsensusResult dspConsensusResult) {
        log.debug("Continue to handle vote conclusion by base node");
    }

    @Override
    public DspConsensusResult getPostponedDspConsensusResult(Hash transactionHash) {
        return postponedDspConsensusResultsMap.get(transactionHash);
    }


}
