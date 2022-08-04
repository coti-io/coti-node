package io.coti.zerospend.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.interfaces.IClusterHelper;
import io.coti.basenode.services.interfaces.IClusterService;
import io.coti.zerospend.data.ZeroSpendTransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SourceStarvationService {

    private static final long SOURCE_STARVATION_CHECK_TASK_DELAY = 500;
    @Autowired
    private IClusterService clusterService;
    @Autowired
    private IClusterHelper clusterHelper;
    @Autowired
    private TransactionCreationService transactionCreationService;

    @Scheduled(fixedDelay = SOURCE_STARVATION_CHECK_TASK_DELAY)
    public void checkSourcesStarvation() {
        //log.debug("Checking Source Starvation");
        Instant now = Instant.now();
        //ConcurrentHashMap<Hash, TransactionData> trustChainConfirmationCluster = clusterService.getCopyTrustChainConfirmationCluster();
        ConcurrentHashMap<Hash, TransactionData> trustChainConfirmationCluster = new ConcurrentHashMap<>(clusterService.getCopyTrustChainConfirmationCluster()); //clusterService.getCopyTrustChainConfirmationCluster();
        List<TransactionData> orphanedZeroSpendSources = new ArrayList<>();

        ConcurrentHashMap<TransactionData, TransactionData> rootSourcePairs = new ConcurrentHashMap<>();
        for (TransactionData transactionData : trustChainConfirmationCluster.values()) {
            boolean isParentInCluster = clusterHelper.isParentInCluster(transactionData, trustChainConfirmationCluster);
            if (!isParentInCluster) {
                clusterHelper.mapPathsToSources(transactionData, trustChainConfirmationCluster, null, rootSourcePairs, orphanedZeroSpendSources);
            }
        }
        List<TransactionData> orphanedStarvationSources = orphanedZeroSpendSources.stream().filter(p -> !ZeroSpendTransactionType.GENESIS.toString().equals(p.getTransactionDescription())).collect(Collectors.toList());

        createNewStarvationZeroSpendTransactions(now, rootSourcePairs, orphanedStarvationSources);
        createNewGenesisZeroSpendTransactions();
    }

    private void createNewGenesisZeroSpendTransactions() {
        ArrayList<HashSet<Hash>> sourceListsByTrustScore = clusterService.getSourceSetsByTrustScore();
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

    private void createNewStarvationZeroSpendTransactions(Instant now, ConcurrentHashMap<TransactionData, TransactionData> rootSourcePairs, List<TransactionData> orphanedStarvationSources) {
        List<TransactionData> sourcesAttached = new ArrayList<>();
        List<TransactionData> zeroSpendSourcesAttached = new ArrayList<>();
        List<TransactionData> newlyCreatedZeroSpends = new ArrayList<>();
        for (Map.Entry<TransactionData, TransactionData> rootSourcePair : rootSourcePairs.entrySet()) {
            TransactionData rootTransactionData = rootSourcePair.getKey();
            TransactionData sourceTransactionData = rootSourcePair.getValue();
            if (!sourcesAttached.contains(sourceTransactionData)) {
                long minimumWaitingTimeInMilliseconds = clusterHelper.getMinimumWaitTimeInMilliseconds(rootTransactionData);
                long actualWaitingTimeInMilliseconds = Duration.between(rootTransactionData.getAttachmentTime(), now).toMillis();
                //todo
                log.debug("Waiting transaction: {}. Time without attachment: {}, Minimum wait time: {}", rootTransactionData.getHash(), actualWaitingTimeInMilliseconds, minimumWaitingTimeInMilliseconds);
                //log.debug("Waiting transaction: {}. Time without attachment: {}, Minimum wait time: {}", rootTransactionData.getHash(), millisecondsToMinutes(actualWaitingTimeInMilliseconds), millisecondsToMinutes(minimumWaitingTimeInMilliseconds));
                //if (actualWaitingTimeInMilliseconds > minimumWaitingTimeInMilliseconds) {
                    createJointSourceStarvationZeroSpendTransaction(newlyCreatedZeroSpends, sourceTransactionData, sourcesAttached,
                            rootSourcePairs, orphanedStarvationSources, zeroSpendSourcesAttached);
                //} else {
               //     rootSourcePairs.remove(rootTransactionData);
               // }
            }
        }
        for (TransactionData newZeroSpend : newlyCreatedZeroSpends) {
            transactionCreationService.attachAndSendZeroSpendTransaction(newZeroSpend);
        }
    }

    private void createJointSourceStarvationZeroSpendTransaction(List<TransactionData> newlyCreatedZeroSpends, TransactionData sourceTransactionData, List<TransactionData> sourcesAttached,
                                                                 ConcurrentHashMap<TransactionData, TransactionData> rootSourcePairs, List<TransactionData> orphanedStarvationSources,
                                                                 List<TransactionData> zeroSpendSourcesAttached) {
        TransactionData zeroSpendTransaction = transactionCreationService.createNewStarvationZeroSpendTransaction(sourceTransactionData);
        if (zeroSpendTransaction == null) {
            return;
        }
        newlyCreatedZeroSpends.add(zeroSpendTransaction);
        sourcesAttached.add(sourceTransactionData);
        List<TransactionData> possibleSources = clusterService.findSources(zeroSpendTransaction);
        possibleSources = possibleSources.stream().filter(p -> !p.getHash().equals(sourceTransactionData.getHash())).collect(Collectors.toList());
        possibleSources = possibleSources.stream().filter(p -> !ZeroSpendTransactionType.GENESIS.toString().equals(p.getTransactionDescription())).collect(Collectors.toList());
        for (TransactionData possibleOtherParent : rootSourcePairs.values()) {
            if (possibleSources.stream().anyMatch(tx -> tx.getHash().equals(possibleOtherParent.getHash()))) {
                zeroSpendTransaction.setRightParentHash(possibleOtherParent.getHash());
                if (possibleOtherParent.getSenderTrustScore() > zeroSpendTransaction.getSenderTrustScore()) {
                    zeroSpendTransaction.setSenderTrustScore(possibleOtherParent.getSenderTrustScore());
                }
                sourcesAttached.add(possibleOtherParent);
                break;
            }
        }
        if (zeroSpendTransaction.getRightParentHash() == null && !orphanedStarvationSources.isEmpty()) {
            for (TransactionData possibleZeroSpendParent : orphanedStarvationSources) {
                if (possibleSources.contains(possibleZeroSpendParent) && !zeroSpendSourcesAttached.contains(possibleZeroSpendParent)) {
                    zeroSpendTransaction.setRightParentHash(possibleZeroSpendParent.getHash());
                    zeroSpendSourcesAttached.add(possibleZeroSpendParent);
                    break;
                }
            }
        }
    }

    private String millisecondsToMinutes(long milliseconds) {
        return new SimpleDateFormat("mm:ss").format(new Date(milliseconds));
    }
}
