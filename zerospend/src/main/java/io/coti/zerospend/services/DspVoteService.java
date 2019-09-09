package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
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
    private IPropagationPublisher propagationPublisher;
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

    @Override
    public void init() {
        transactionHashToVotesListMapping = new ConcurrentHashMap<>();
        super.init();
    }

    public void preparePropagatedTransactionForVoting(TransactionData transactionData) {
        List<Hash> dspHashList = new LinkedList<>();
        networkService.getMapFromFactory(NodeType.DspNode).forEach((hash, node) ->
                dspHashList.add(node.getHash())
        );
        log.debug("Received new transaction. Live DSP Nodes: {}", dspHashList);
        TransactionVoteData transactionVoteData = new TransactionVoteData(transactionData.getHash(), dspHashList);
        transactionVotes.put(transactionVoteData);
        transactionHashToVotesListMapping.put(transactionData.getHash(), new LinkedList<>());
    }

    public void receiveDspVote(TransactionDspVote transactionDspVote) {
        log.debug("Received new Dsp Vote: Sender = {} , Transaction = {}", transactionDspVote.getVoterDspHash(), transactionDspVote.getTransactionHash());
        Hash transactionHash = transactionDspVote.getTransactionHash();
        synchronized (transactionHash.toHexString()) {
            TransactionVoteData transactionVoteData = transactionVotes.getByHash(transactionHash);
            if (transactionVoteData == null) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                transactionVoteData = transactionVotes.getByHash(transactionHash);
                if (transactionVoteData == null) {
                    throw new DspVoteException(String.format("Transaction {} does not exist for dsp vote", transactionHash));
                }
            }

            if (!transactionVoteData.getLegalVoterDspHashes().contains(transactionDspVote.getVoterDspHash())) {
                throw new DspVoteException(String.format("Unauthorized Dsp vote received. Sender =  {}, Transaction =  {}", transactionDspVote.getVoterDspHash(), transactionDspVote.getTransactionHash()));
            }

            if (!transactionDspVoteCrypto.verifySignature(transactionDspVote)) {
                throw new DspVoteException(String.format("Invalid vote signature. Sender =  {}, Transaction = {}", transactionDspVote.getVoterDspHash(), transactionDspVote.getTransactionHash()));
            }
            if (transactionHashToVotesListMapping.get(transactionHash) == null) {
                log.debug("Dsp vote result already published");
                return;
            }
            log.debug("Adding new vote: {}", transactionDspVote);
            transactionHashToVotesListMapping.get(transactionHash).add(new DspVote(transactionDspVote));
        }
    }

    @Scheduled(fixedDelay = 1000)
    private void sumAndSaveVotes() {
        for (Map.Entry<Hash, List<DspVote>> transactionHashToVotesListEntrySet :
                transactionHashToVotesListMapping.entrySet()) {
            synchronized (transactionHashToVotesListEntrySet.getKey().toHexString()) {
                if (transactionHashToVotesListEntrySet.getValue() != null && transactionHashToVotesListEntrySet.getValue().size() > 0) {
                    Hash transactionHash = transactionHashToVotesListEntrySet.getKey();
                    TransactionVoteData currentTransactionVoteData = transactionVotes.getByHash(transactionHash);
                    Map<Hash, DspVote> mapHashToDspVote = currentTransactionVoteData.getDspHashToVoteMapping();
                    transactionHashToVotesListEntrySet.getValue().forEach(dspVote -> mapHashToDspVote.putIfAbsent(dspVote.getVoterDspHash(), dspVote));
                    if (isPositiveMajorityAchieved(currentTransactionVoteData)) {
                        publishDecision(transactionHash, mapHashToDspVote, true);
                        log.debug("Valid vote majority achieved for transaction {}", currentTransactionVoteData.getHash());
                    } else if (isNegativeMajorityAchieved(currentTransactionVoteData)) {
                        publishDecision(transactionHash, mapHashToDspVote, false);
                        log.debug("Invalid vote majority achieved for transaction {}", currentTransactionVoteData.getHash());
                    } else {
                        log.debug("Undecided majority for transaction {}", currentTransactionVoteData.getHash());
                    }
                    transactionVotes.put(currentTransactionVoteData);
                }
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