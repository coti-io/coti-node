package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.DspConsensusCrypto;
import io.coti.basenode.crypto.DspVoteCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.model.TransactionVotes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeDspVoteService;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
    private DspVoteCrypto dspVoteCrypto;
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
        networkService.getNetworkDetails().getDspNetworkNodesList().forEach(node->
            dspHashList.add(node.getHash())
        );
        log.debug("Received new transaction. Live DSP Nodes: {}", dspHashList);
        TransactionVoteData transactionVoteData = new TransactionVoteData(transactionData.getHash(), dspHashList);
        transactionVotes.put(transactionVoteData);
        transactionHashToVotesListMapping.put(transactionData.getHash(), new LinkedList<>());
    }

    public String receiveDspVote(DspVote dspVote) {
        log.debug("Received new Dsp Vote: Sender = {} , Transaction = {}", dspVote.getVoterDspHash(), dspVote.getTransactionHash());
        Hash transactionHash = dspVote.getTransactionHash();
        synchronized (transactionHash.toHexString()) {
            TransactionVoteData transactionVoteData = transactionVotes.getByHash(transactionHash);
            if (transactionVoteData == null) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                transactionVoteData = transactionVotes.getByHash(transactionHash);
                if (transactionVoteData == null) {
                    return "Transaction does not exist";
                }
            }

            if (!transactionVoteData.getLegalVoterDspHashes().contains(dspVote.getVoterDspHash())) {
                log.error("Unauthorized Dsp vote received. Sender =  {}, Transaction =  {}", dspVote.getVoterDspHash(), dspVote.getTransactionHash());
                log.error("Keyset count: " + transactionVoteData.getDspHashToVoteMapping().keySet().size());
                transactionVoteData.getDspHashToVoteMapping().keySet().forEach(hash -> log.info(hash.toHexString()));
                return "Unauthorized";
            }

            if (!dspVoteCrypto.verifySignature(dspVote)) {
                log.error("Invalid vote signature. Sender =  {}, Transaction = {}", dspVote.getVoterDspHash(), dspVote.getTransactionHash());
                return "Invalid Signature";
            }
            if (transactionHashToVotesListMapping.get(transactionHash) == null) {
                log.debug("Transaction Not existing in mapping!!");
                return "Vote already processed";
            }
            log.debug("Adding new vote: {}", dspVote);
            transactionHashToVotesListMapping.get(transactionHash).add(dspVote);
        }
        return "Ok";
    }

    @Scheduled(fixedDelay = 1000)
    private void sumAndSaveVotes() {
        for (Map.Entry<Hash, List<DspVote>> transactionHashToVotesListEntrySet :
                transactionHashToVotesListMapping.entrySet()) {
            synchronized (transactionHashToVotesListEntrySet.getKey().toHexString()) {
                if (transactionHashToVotesListEntrySet.getValue() != null && transactionHashToVotesListEntrySet.getValue().size() > 0) {
                    Hash transactionHash = transactionHashToVotesListEntrySet.getKey();
                    TransactionVoteData currentVotes = transactionVotes.getByHash(transactionHash);
                    Map<Hash, DspVote> mapHashToDspVote = currentVotes.getDspHashToVoteMapping();
                    transactionHashToVotesListEntrySet.getValue().forEach(dspVote -> mapHashToDspVote.putIfAbsent(dspVote.getVoterDspHash(), dspVote));
                    if (isPositiveMajorityAchieved(currentVotes)) {
                        publishDecision(transactionHash, mapHashToDspVote, true);
                        log.debug("Valid vote majority achieved for: {}", currentVotes.getHash());
                    } else if (isNegativeMajorityAchieved(currentVotes)) {
                        publishDecision(transactionHash, mapHashToDspVote, false);
                        log.debug("Invalid vote majority achieved for: {}", currentVotes.getHash());
                    } else {
                        log.debug("Undecided majority: {}", currentVotes.getHash());
                    }
                    transactionVotes.put(currentVotes);
                }
            }
        }
    }

    private void publishDecision(Hash transactionHash, Map<Hash, DspVote> mapHashToDspVote, boolean isLegalTransaction) {
        TransactionData transactionData = transactions.getByHash(transactionHash);
        DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
        dspConsensusResult.setDspConsensus(isLegalTransaction);
        List<DspVote> dspVotes = new LinkedList<>();
        mapHashToDspVote.forEach((hash, dspVote) -> dspVotes.add(dspVote));
        dspConsensusResult.setDspVotes(dspVotes);
        setIndexForDspResult(transactionData, dspConsensusResult);
        confirmationService.setDspcToTrue(dspConsensusResult);
        propagationPublisher.propagate(dspConsensusResult, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode));
        transactionHashToVotesListMapping.remove(transactionHash);
    }

    public synchronized void setIndexForDspResult(TransactionData transactionData, DspConsensusResult dspConsensusResult) {
        dspConsensusResult.setIndex(transactionIndexService.getLastTransactionIndexData().getIndex() + 1);
        dspConsensusResult.setIndexingTime(new Date());
        dspConsensusCrypto.signMessage(dspConsensusResult);
        transactionData.setDspConsensusResult(dspConsensusResult);
        transactionIndexService.insertNewTransactionIndex(transactionData);
    }

    private boolean isPositiveMajorityAchieved(TransactionVoteData currentVotes) {
        long positiveVotersCount = currentVotes
                .getDspHashToVoteMapping()
                .values()
                .stream()
                .filter(vote -> vote.isValidTransaction())
                .count();
        long totalVotersCount = currentVotes.getLegalVoterDspHashes().size();
        if (positiveVotersCount > totalVotersCount / 2) {
            return true;
        }
        return false;
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
        if (negativeVotersCount > totalVotersCount / 2) {
            return true;
        }
        return false;
    }

    @Override
    public void continueHandleVoteConclusion(DspConsensusResult dspConsensusResult) {

    }

   
}
