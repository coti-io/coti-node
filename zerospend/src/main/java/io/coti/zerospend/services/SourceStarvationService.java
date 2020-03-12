package io.coti.zerospend.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.services.interfaces.IClusterHelper;
import io.coti.basenode.services.interfaces.IClusterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SourceStarvationService {

    private static final long MINIMUM_WAIT_TIME_IN_SECONDS = 10;
    private static final long SOURCE_STARVATION_CHECK_TASK_DELAY = 10000;
    @Autowired
    private IClusterService clusterService;
    @Autowired
    private IClusterHelper clusterHelper;
    @Autowired
    private TransactionCreationService transactionCreationService;

    @Scheduled(fixedDelay = SOURCE_STARVATION_CHECK_TASK_DELAY)
    public void checkSourcesStarvation() {
        log.debug("Checking Source Starvation");
        Instant now = Instant.now();
        ConcurrentHashMap<Hash, TransactionData> trustChainConfirmationCluster = clusterService.getCopyTrustChainConfirmationCluster();
        LinkedList<TransactionData> topologicalOrderedGraph = new LinkedList<>();
        ConcurrentHashMap<Hash, Instant> nonZeroSpendChainTransactions = new ConcurrentHashMap<>();

        clusterHelper.sortByTopologicalOrder(trustChainConfirmationCluster, topologicalOrderedGraph);

        createNewStarvationZeroSpendTransactions(now, topologicalOrderedGraph, nonZeroSpendChainTransactions);

        createNewGenesisZeroSpendTransactions();
    }

    private void createNewGenesisZeroSpendTransactions() {
        List<Set<TransactionData>> sourceListsByTrustScore = clusterService.getSourceListsByTrustScore();
        boolean isTrustScoreRangeContainsSource = false;
        for (int i = 1; i <= 100; i++) {
            if (!sourceListsByTrustScore.get(i).isEmpty()) {
                isTrustScoreRangeContainsSource = true;
            }
            if (i % 10 == 0) {
                if (!isTrustScoreRangeContainsSource) {
                    transactionCreationService.createNewGenesisZeroSpendTransaction(i);
                }
                isTrustScoreRangeContainsSource = false;
            }
        }
    }

    private void createNewStarvationZeroSpendTransactions(Instant now, LinkedList<TransactionData> topologicalOrderedGraph, ConcurrentHashMap<Hash, Instant> nonZeroSpendChainTransactions) {
        for (int i = topologicalOrderedGraph.size() - 1; i >= 0; i--) {
            TransactionData transactionData = topologicalOrderedGraph.get(i);
            if (!transactionData.getType().equals(TransactionType.ZeroSpend)) {
                nonZeroSpendChainTransactions.put(transactionData.getHash(), transactionData.getAttachmentTime());
            }
            parentInNonZeroChain(transactionData.getLeftParentHash(), transactionData.getHash(), nonZeroSpendChainTransactions);
            parentInNonZeroChain(transactionData.getRightParentHash(), transactionData.getHash(), nonZeroSpendChainTransactions);

            if (transactionData.getChildrenTransactionHashes().isEmpty() && nonZeroSpendChainTransactions.containsKey(transactionData.getHash())) {
                long minimumWaitingTimeInMilliseconds = (long) (100 - transactionData.getSenderTrustScore() + MINIMUM_WAIT_TIME_IN_SECONDS) * 1000;
                long actualWaitingTimeInMilliseconds = Duration.between(nonZeroSpendChainTransactions.get(transactionData.getHash()), now).toMillis();
                log.debug("Waiting transaction: {}. Time without attachment: {}, Minimum wait time: {}", transactionData.getHash(), millisecondsToMinutes(actualWaitingTimeInMilliseconds), millisecondsToMinutes(minimumWaitingTimeInMilliseconds));
                if (actualWaitingTimeInMilliseconds > minimumWaitingTimeInMilliseconds) {
                    transactionCreationService.createNewStarvationZeroSpendTransaction(transactionData);
                }
            }
        }
    }

    private void parentInNonZeroChain(Hash parentHash, Hash transactionHash, ConcurrentHashMap<Hash, Instant> nonZeroSpendChainTransactions) {
        if (parentHash != null) {
            Instant parentAttachmentTime = nonZeroSpendChainTransactions.get(parentHash);
            if (parentAttachmentTime != null) {
                nonZeroSpendChainTransactions.computeIfPresent(transactionHash, (transactionHashKey, transactionAttachmentTime) ->
                        transactionAttachmentTime.isAfter(parentAttachmentTime) ? parentAttachmentTime : transactionAttachmentTime
                );
                nonZeroSpendChainTransactions.putIfAbsent(transactionHash, parentAttachmentTime);
            }
        }
    }

    private String millisecondsToMinutes(long milliseconds) {
        return new SimpleDateFormat("mm:ss").format(new Date(milliseconds));
    }
}