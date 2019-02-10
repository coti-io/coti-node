package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.DspConsensusCrypto;
import io.coti.basenode.crypto.DspVoteCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.model.TransactionVotes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeDspVoteService;
import io.coti.basenode.services.TransactionIndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class DspVoteService extends BaseNodeDspVoteService {

    private static final String NODE_HASH_ENDPOINT = "/nodeHash";
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
    @Value("#{'${dsp.server.addresses}'.split(',')}")
    private List<String> dspServerAddresses;
    private List<Hash> currentLiveDspNodes;
    private Thread sumAndSaveVotesThread;

    @Override
    public void init() {
        super.init();
        sumAndSaveVotesThread = new Thread(() -> sumAndSaveVotes());
        sumAndSaveVotesThread.start();
    }

    public void preparePropagatedTransactionForVoting(TransactionData transactionData) {
        log.debug("Received new transaction. Live DSP Nodes: ");
        TransactionVoteData transactionVoteData = new TransactionVoteData(transactionData.getHash(), currentLiveDspNodes);
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

    @Scheduled(fixedDelay = 10000, initialDelay = 5000)
    private void updateLiveDspNodesList() {
        List<Hash> onlineDspHashes = new LinkedList<>();
        RestTemplate restTemplate = new RestTemplate();
        for (String dspServerAddress : dspServerAddresses) {
            try {
                Hash voterHash = restTemplate.getForObject(dspServerAddress + NODE_HASH_ENDPOINT, Hash.class);
                if (voterHash == null) {
                    log.error("Voter hash received is null: {}", dspServerAddress);
                } else {
                    onlineDspHashes.add(voterHash);
                }
            } catch (RestClientException e) {
                log.error("Unresponsive Dsp Node: {}", dspServerAddress);
            }
        }
        if (onlineDspHashes.isEmpty()) {
            log.error("No Dsp Nodes are online...");
        }
        currentLiveDspNodes = onlineDspHashes;
        log.info("Updated live dsp nodes list. Count: {}", currentLiveDspNodes.size());
    }

    private void sumAndSaveVotes() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                for (Map.Entry<Hash, List<DspVote>> transactionHashToVotesListEntrySet :
                        transactionHashToVotesListMapping.entrySet()) {
                    sumAndSaveVotesHandler(transactionHashToVotesListEntrySet);
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void sumAndSaveVotesHandler(Map.Entry<Hash, List<DspVote>> transactionHashToVotesListEntrySet) {
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

    private void publishDecision(Hash transactionHash, Map<Hash, DspVote> mapHashToDspVote, boolean isLegalTransaction) {
        TransactionData transactionData = transactions.getByHash(transactionHash);
        DspConsensusResult dspConsensusResult = new DspConsensusResult(transactionData.getHash());
        dspConsensusResult.setDspConsensus(isLegalTransaction);
        List<DspVote> dspVotes = new LinkedList<>();
        mapHashToDspVote.forEach((hash, dspVote) -> dspVotes.add(dspVote));
        dspConsensusResult.setDspVotes(dspVotes);
        setIndexForDspResult(transactionData, dspConsensusResult);
        confirmationService.setDspcToTrue(dspConsensusResult);
        propagationPublisher.propagate(dspConsensusResult, Arrays.asList(NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer));
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

    public ConcurrentMap<Hash, List<DspVote>> getTransactionHashToVotesListMapping() {
        return transactionHashToVotesListMapping;
    }

    public void stopSumAndSaveVotes() {
        log.info("Shutting down {}", this.getClass().getSimpleName());
        sumAndSaveVotesThread.interrupt();
        try {
           sumAndSaveVotesThread.join();
        } catch (InterruptedException e) {
            log.error("Interrupted shutdown {}", this.getClass().getSimpleName());
        }
    }

    public void startSumAndSaveVotes() {
        log.info("Restarting {}", this.getClass().getSimpleName());
        sumAndSaveVotesThread = new Thread(() -> sumAndSaveVotes());
        sumAndSaveVotesThread.start();
    }
}