package io.coti.zerospend.services;

import io.coti.basenode.crypto.DspConsensusCrypto;
import io.coti.basenode.crypto.TransactionDspVoteCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.DspVoteException;
import io.coti.basenode.model.TransactionVotes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeDspVoteService;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Service
public class DspVoteService extends BaseNodeDspVoteService {

    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private TransactionVotes transactionVotes;
    @Autowired
    private Transactions transactions;
    @Autowired
    private TransactionDspVoteCrypto transactionDspVoteCrypto;
    @Autowired
    private DspConsensusCrypto dspConsensusCrypto;
    @Autowired
    private INetworkService networkService;
    private ConcurrentMap<Hash, List<DspVote>> transactionHashToVotesListMapping;

    private Map<Hash, Hash> lockVotedTransactionRecordHashMap = new ConcurrentHashMap<>();
    private Map<Hash, HashSet<TransactionDspVote>> missingTransactionsAwaitingHandling;

    @Override
    public void init() {
        transactionHashToVotesListMapping = new ConcurrentHashMap<>();
        super.init();
        missingTransactionsAwaitingHandling = new ConcurrentHashMap<>();
    }

    protected Hash addLockToLockMap(Hash hash) {
        return addLockToLockMap(lockVotedTransactionRecordHashMap, hash);
    }

    private Hash addLockToLockMap(Map<Hash, Hash> locksIdentityMap, Hash hash) {
        synchronized (locksIdentityMap) {
            locksIdentityMap.putIfAbsent(hash, hash);
            return locksIdentityMap.get(hash);
        }
    }

    protected void removeLockFromLocksMap(Hash hash) {
        removeLockFromLocksMap(lockVotedTransactionRecordHashMap, hash);
    }

    private void removeLockFromLocksMap(Map<Hash, Hash> locksIdentityMap, Hash hash) {
        synchronized (locksIdentityMap) {
            Hash hashLock = locksIdentityMap.get(hash);
            if (hashLock != null) {
                locksIdentityMap.remove(hash);
            }
        }
    }

    public void preparePropagatedTransactionForVoting(TransactionData transactionData) {
        List<Hash> dspHashList = new LinkedList<>();
        networkService.getMapFromFactory(NodeType.DspNode).forEach((hash, node) ->
                dspHashList.add(node.getHash())
        );
        log.debug("Received new transaction. Live DSP Nodes: {}", dspHashList);
        Hash transactionDataHash = transactionData.getHash();
        TransactionVoteData transactionVoteData = new TransactionVoteData(transactionDataHash, dspHashList);
        synchronized (addLockToLockMap(transactionDataHash)) {
            Hash transactionVoteDataHash = transactionVoteData.getHash();
            boolean isNewTransaction = (transactionVotes.getByHash(transactionVoteDataHash) == null);
            transactionVotes.put(transactionVoteData);
            transactionHashToVotesListMapping.put(transactionDataHash, new LinkedList<>());

            HashSet<TransactionDspVote> transactionDspVotesAwaitingHandling = missingTransactionsAwaitingHandling.get(transactionVoteDataHash);
            if (isNewTransaction && transactionDspVotesAwaitingHandling != null && !transactionDspVotesAwaitingHandling.isEmpty()) {
                transactionDspVotesAwaitingHandling.forEach(transactionDspVote -> handlePostponedTransactionDspVote(transactionDspVote, transactionDataHash));
                missingTransactionsAwaitingHandling.remove(transactionVoteDataHash);
            }
        }
        removeLockFromLocksMap(transactionDataHash);
    }

    private void handlePostponedTransactionDspVote(TransactionDspVote transactionDspVote, Hash transactionHash) {
        TransactionVoteData transactionVoteData = transactionVotes.getByHash(transactionHash);
        Hash voterDspHash = transactionDspVote.getVoterDspHash();
        Hash transactionDspVoteTransactionHash = transactionDspVote.getTransactionHash();
        if (!transactionVoteData.getLegalVoterDspHashes().contains(voterDspHash)) {
            log.error("Unauthorized Dsp vote received. Sender =  {}, Transaction =  {}", voterDspHash, transactionDspVoteTransactionHash);
            return;
        }
        if (!transactionDspVoteCrypto.verifySignature(transactionDspVote)) {
            log.error("Invalid vote signature. Sender =  {}, Transaction = {}", voterDspHash, transactionDspVoteTransactionHash);
            return;
        }
        if (transactionHashToVotesListMapping.get(transactionHash) == null) {
            log.debug("Dsp vote result for transaction {} already published", transactionDspVoteTransactionHash);
            return;
        }
        log.debug("Adding new vote: {}", transactionDspVote);
        transactionHashToVotesListMapping.get(transactionHash).add(new DspVote(transactionDspVote));
    }

    public void receiveDspVote(TransactionDspVote transactionDspVote) {
        Hash transactionHash = transactionDspVote.getTransactionHash();
        Hash voterDspHash = transactionDspVote.getVoterDspHash();
        log.debug("Received new Dsp Vote: Sender = {} , Transaction = {}", voterDspHash, transactionHash);
        synchronized (addLockToLockMap(transactionHash)) {
            TransactionVoteData transactionVoteData = transactionVotes.getByHash(transactionHash);
            if (transactionVoteData == null) {
                missingTransactionsAwaitingHandling.putIfAbsent(transactionHash, new HashSet<>());
                missingTransactionsAwaitingHandling.get(transactionHash).add(transactionDspVote);
                log.info("Transaction {} does not exist for dsp vote. Vote processing is delayed.", transactionHash);
                return;
            }

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
        removeLockFromLocksMap(transactionHash);
    }

    @Scheduled(fixedDelay = 1000)
    private void sumAndSaveVotes() {
        for (Map.Entry<Hash, List<DspVote>> transactionHashToVotesListEntrySet :
                transactionHashToVotesListMapping.entrySet()) {
            Hash transactionHash = transactionHashToVotesListEntrySet.getKey();
            synchronized (addLockToLockMap(transactionHash)) {
                List<DspVote> transactionHashToVotesListEntrySetValue = transactionHashToVotesListEntrySet.getValue();
                if (transactionHashToVotesListEntrySetValue != null && !transactionHashToVotesListEntrySetValue.isEmpty()) {
                    TransactionVoteData currentTransactionVoteData = transactionVotes.getByHash(transactionHash);
                    Map<Hash, DspVote> mapHashToDspVote = currentTransactionVoteData.getDspHashToVoteMapping();
                    transactionHashToVotesListEntrySetValue.forEach(dspVote -> mapHashToDspVote.putIfAbsent(dspVote.getVoterDspHash(), dspVote));
                    if (isPositiveMajorityAchieved(currentTransactionVoteData)) {
                        publishDecision(transactionHash, mapHashToDspVote, true);
                        log.debug("Valid vote majority achieved for transaction {}", currentTransactionVoteData.getHash());
                    } else if (isNegativeMajorityAchieved(currentTransactionVoteData)) {
                        publishDecision(transactionHash, mapHashToDspVote, false);
                        log.debug("Invalid vote majority achieved for transaction {}", currentTransactionVoteData.getHash());
                    } else {
                        log.debug("Undecided majority for transaction {}", currentTransactionVoteData.getHash());
                    }
                }
            }
            removeLockFromLocksMap(transactionHash);
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
                .filter(vote -> vote.isValidTransaction())
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
        long totalVotersCount = currentVotes
                .getDspHashToVoteMapping()
                .size();
        return negativeVotersCount > totalVotersCount / 2;
    }
}