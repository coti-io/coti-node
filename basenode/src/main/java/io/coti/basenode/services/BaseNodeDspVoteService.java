package io.coti.basenode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.DspConsensusResultException;
import io.coti.basenode.services.interfaces.IDspVoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.coti.basenode.services.BaseNodeServiceManager.*;

@Slf4j
@Service
public class BaseNodeDspVoteService implements IDspVoteService {

    private Map<Hash, DspConsensusResult> postponedDspConsensusResultsMap;

    public void init() {
        postponedDspConsensusResultsMap = new ConcurrentHashMap<>();
        log.info("{} is up", this.getClass().getSimpleName());
    }

    public synchronized void handleVoteConclusion(DspConsensusResult dspConsensusResult) {
        try {
            log.info("Received DspConsensus result for transaction: {}", dspConsensusResult.getHash());
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
        transactionPropagationCheckService.removeTransactionHashFromUnconfirmed(transactionData.getHash());
        if (transactionData.getDspConsensusResult() != null) {
            log.debug("DspConsensus result already exists for transaction {}", dspConsensusResult.getHash());
            return;
        }
        if (dspConsensusResult.isDspConsensus()) {
            log.debug("Valid vote conclusion received for transaction: {}", dspConsensusResult.getHash());
        } else {
            log.debug("Invalid vote conclusion received for transaction: {}", dspConsensusResult.getHash());
        }

        log.debug("DspConsensus result for transaction: Hash= {}, DspVoteResult= {}, Index= {}", dspConsensusResult.getHash(), dspConsensusResult.isDspConsensus(), dspConsensusResult.getIndex());
    }

    protected void continueHandleVoteConclusion(DspConsensusResult dspConsensusResult) {
        // implemented by the sub classes
    }

    @Override
    public DspConsensusResult getPostponedDspConsensusResult(Hash transactionHash) {
        return postponedDspConsensusResultsMap.get(transactionHash);
    }

    @Override
    public void handleDspConsensusResultResend(NodeResendDcrData nodeResendDcrData) {
        try {
            log.info("Received request for missing dsp consensus results for node: {}", nodeResendDcrData.getNodeHash());
            long startIndex = nodeResendDcrData.getFirstMissedIndex();
            long endIndex = nodeResendDcrData.getInRangeLastMissedIndex();
            while (startIndex <= endIndex) {
                TransactionIndexData transactionIndexData = transactionIndexes.getByHash(new Hash(startIndex));
                if (transactionIndexData == null) {
                    log.error("Error, there is no TransactionIndexData for index: {}", startIndex);
                    return;
                }
                TransactionData transaction = transactions.getByHash(transactionIndexData.getTransactionHash());
                if (transaction != null && transaction.getDspConsensusResult() != null) {
                    propagationPublisher.propagate(transaction.getDspConsensusResult(),
                            Collections.singletonList(nodeResendDcrData.getNodeType()));
                } else {
                    log.error("Error, there is no DSP Consensus Result for transaction: {}", transactionIndexData.getTransactionHash());
                    return;
                }
                startIndex++;
            }
        } catch (Exception e) {
            log.error("Error occurred in {}: {}", e.getClass().getName(), e.getMessage());
        }
    }

    @Override
    public void setIndexForDspResult(TransactionData transactionData, DspConsensusResult dspConsensusResult) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void publishDecision(Hash hash) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void preparePropagatedTransactionForVoting(TransactionData transactionData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void receiveDspVote(TransactionDspVote data) {
        throw new UnsupportedOperationException();
    }
}
