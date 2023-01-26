package io.coti.zerospend.services;

import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.DspVoteException;
import io.coti.basenode.services.BaseNodeDspVoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.coti.zerospend.services.NodeServiceManager.*;

@Slf4j
@Service
@Primary
public class DspVoteService extends BaseNodeDspVoteService {

    private ConcurrentMap<Hash, List<DspVote>> transactionHashToVotesListMapping;
    private final LockData transactionHashLockData = new LockData();
    private Map<Hash, HashSet<TransactionDspVote>> missingTransactionsAwaitingHandling;

    @Override
    public void init() {
        transactionHashToVotesListMapping = new ConcurrentHashMap<>();
        missingTransactionsAwaitingHandling = new ConcurrentHashMap<>();
        super.init();
    }

    public void preparePropagatedTransactionForVoting(TransactionData transactionData) {
        List<Hash> dspHashList = new LinkedList<>();
        networkService.getMapFromFactory(NodeType.DspNode).forEach((hash, node) ->
                dspHashList.add(node.getHash())
        );
        log.debug("Received new transaction. Live DSP Nodes: {}", dspHashList);
        Hash transactionHash = transactionData.getHash();
        TransactionVoteData transactionVoteData = new TransactionVoteData(transactionHash, dspHashList);
        try {
            synchronized (transactionHashLockData.addLockToLockMap(transactionHash)) {
                transactionVotes.put(transactionVoteData);
                transactionHashToVotesListMapping.put(transactionHash, new LinkedList<>());

                HashSet<TransactionDspVote> transactionDspVotesAwaitingHandling = missingTransactionsAwaitingHandling.get(transactionHash);
                if (transactionDspVotesAwaitingHandling != null && !transactionDspVotesAwaitingHandling.isEmpty()) {
                    transactionDspVotesAwaitingHandling.forEach(transactionDspVote -> handlePostponedTransactionDspVote(transactionDspVote, transactionVoteData));
                    missingTransactionsAwaitingHandling.remove(transactionHash);
                }
            }
        } finally {
            transactionHashLockData.removeLockFromLocksMap(transactionHash);
        }

    }

    private void handlePostponedTransactionDspVote(TransactionDspVote transactionDspVote, TransactionVoteData transactionVoteData) {
        try {
            Hash transactionHash = transactionDspVote.getHash();
            Hash voterDspHash = transactionDspVote.getVoterDspHash();
            log.debug("Handling postponed Dsp Vote: Sender = {} , Transaction = {}", voterDspHash, transactionHash);
            handleDspVote(transactionDspVote, transactionVoteData, voterDspHash, transactionHash);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private void handleDspVote(TransactionDspVote transactionDspVote, TransactionVoteData transactionVoteData, Hash voterDspHash, Hash transactionHash) {
        if (!transactionVoteData.getLegalVoterDspHashes().contains(voterDspHash)) {
            throw new DspVoteException(String.format("Unauthorized Dsp vote received. Sender =  %s, Transaction =  %s", voterDspHash, transactionHash));
        }
        if (!transactionDspVoteCrypto.verifySignature(transactionDspVote)) {
            throw new DspVoteException(String.format("Invalid vote signature. Sender =  %s, Transaction = %s", voterDspHash, transactionHash));
        }
        if (transactionHashToVotesListMapping.get(transactionHash) == null) {
            log.debug("Dsp vote result already published");
            return;
        }
        log.debug("Adding new vote: {}", transactionDspVote);
        transactionHashToVotesListMapping.get(transactionHash).add(new DspVote(transactionDspVote));
    }

    public void receiveDspVote(TransactionDspVote transactionDspVote) {
        Hash transactionHash = transactionDspVote.getHash();
        Hash voterDspHash = transactionDspVote.getVoterDspHash();
        log.info("Received new Dsp Vote: Sender = {} , Transaction = {}", voterDspHash, transactionHash);
        try {
            synchronized (transactionHashLockData.addLockToLockMap(transactionHash)) {
                TransactionVoteData transactionVoteData = transactionVotes.getByHash(transactionHash);
                if (transactionVoteData == null) {
                    missingTransactionsAwaitingHandling.computeIfAbsent(transactionHash, key -> new HashSet<>()).add(transactionDspVote);
                    log.debug("Transaction {} does not exist for dsp vote. Vote processing is delayed.", transactionHash);
                    return;
                }

                handleDspVote(transactionDspVote, transactionVoteData, voterDspHash, transactionHash);
            }
        } finally {
            transactionHashLockData.removeLockFromLocksMap(transactionHash);
        }

    }

    @Scheduled(fixedDelay = 1000)
    private void sumAndSaveVotes() {
        for (Map.Entry<Hash, List<DspVote>> transactionHashToVotesListEntrySet :
                transactionHashToVotesListMapping.entrySet()) {
            Hash transactionHash = transactionHashToVotesListEntrySet.getKey();
            try {
                synchronized (transactionHashLockData.addLockToLockMap(transactionHash)) {
                    List<DspVote> dspVotes = transactionHashToVotesListEntrySet.getValue();
                    if (dspVotes != null && !dspVotes.isEmpty()) {
                        TransactionVoteData currentTransactionVoteData = transactionVotes.getByHash(transactionHash);
                        Map<Hash, DspVote> mapHashToDspVote = currentTransactionVoteData.getDspHashToVoteMapping();
                        dspVotes.forEach(dspVote -> mapHashToDspVote.putIfAbsent(dspVote.getVoterDspHash(), dspVote));
                        if (isPositiveMajorityAchieved(currentTransactionVoteData)) {
                            publishDecision(transactionHash, mapHashToDspVote, true);
                            log.debug("Valid vote majority achieved for transaction {}", currentTransactionVoteData.getHash());
                        } else if (isNegativeMajorityAchieved(currentTransactionVoteData)) {
                            publishDecision(transactionHash, mapHashToDspVote, false);
                            log.debug("Invalid vote majority achieved for transaction {}", currentTransactionVoteData.getHash());
                        } else {
                            log.warn("Undecided majority for transaction {}", currentTransactionVoteData.getHash());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Exception at DspVoteService::sumAndSaveVotes(): ", e);
                throw e;
            } finally {
                transactionHashLockData.removeLockFromLocksMap(transactionHash);
            }
        }
    }

    private synchronized void publishDecision(Hash transactionHash, Map<Hash, DspVote> mapHashToDspVote, boolean isLegalTransaction) {
        TransactionData transactionData = transactions.getByHash(transactionHash);
        DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
        dspConsensusResult.setDspConsensus(isLegalTransaction);
        List<DspVote> dspVotes = new LinkedList<>();
        mapHashToDspVote.forEach((hash, dspVote) -> dspVotes.add(dspVote));
        dspConsensusResult.setDspVotes(dspVotes);
        setIndexForDspResult(transactionData, dspConsensusResult);
        confirmationService.setDspcToTrue(dspConsensusResult);
        propagationPublisher.propagate(dspConsensusResult, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer, NodeType.HistoryNode));
        transactionHashToVotesListMapping.remove(transactionHash);
    }

    public synchronized void setIndexForDspResult(TransactionData transactionData, DspConsensusResult dspConsensusResult) {
        dspConsensusResult.setIndex(transactionIndexService.getLastTransactionIndexData().getIndex() + 1);
        dspConsensusResult.setIndexingTime(Instant.now());
        dspConsensusCrypto.signMessage(dspConsensusResult);
        transactionData.setDspConsensusResult(dspConsensusResult);
        transactionIndexService.insertNewTransactionIndex(transactionData);
    }

    public void publishDecision(Hash transactionHash) {
        publishDecision(transactionHash, new HashMap<>(), true);
    }

    private boolean isPositiveMajorityAchieved(TransactionVoteData currentVotes) {
        long positiveVotersCount = currentVotes
                .getDspHashToVoteMapping()
                .values()
                .stream()
                .filter(DspVote::isValidTransaction)
                .count();
        long totalVotersCount = currentVotes.getLegalVoterDspHashes().size();
        return positiveVotersCount > totalVotersCount / 2;
    }

    private boolean isNegativeMajorityAchieved(TransactionVoteData currentVotes) {
        long negativeVotersCount = currentVotes
                .getDspHashToVoteMapping()
                .values()
                .stream()
                .filter(vote -> !vote.isValidTransaction())
                .count();
        long totalVotersCount = currentVotes.getLegalVoterDspHashes().size();
        return negativeVotersCount > totalVotersCount / 2;
    }
}