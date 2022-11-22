package io.coti.zerospend.services;

import io.coti.basenode.constants.BaseNodeMessages;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.data.TrustChainConfirmationResult;
import io.coti.basenode.services.interfaces.IClusterHelper;
import io.coti.basenode.services.interfaces.IClusterService;
import io.coti.zerospend.data.ZeroSpendTransactionType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SourceStarvationService {

    @Autowired
    private IClusterService clusterService;
    @Autowired
    private IClusterHelper clusterHelper;
    @Autowired
    private TransactionCreationService transactionCreationService;

    @PostConstruct
    protected void init() {
        Thread sourcesStarvationThread = new Thread(this::checkSourcesStarvation, "SOURCES-STARVATION CHECK");
        sourcesStarvationThread.start();
    }

    public void checkSourcesStarvation() {
        BlockingQueue<TrustChainConfirmationResult> trustChainConfirmationResults = clusterService.getTrustChainConfirmationResults();
        while (!Thread.currentThread().isInterrupted()) {
            Map<Hash, Double> transactionTrustChainTrustScoreMap;
            LinkedList<TransactionData> topologicalOrderedGraph;
            try {
                TrustChainConfirmationResult trustChainConfirmationResult = trustChainConfirmationResults.take();
                transactionTrustChainTrustScoreMap = new HashMap<>(trustChainConfirmationResult.getTransactionTrustChainTrustScoreMap());
                topologicalOrderedGraph = new LinkedList<>(trustChainConfirmationResult.getTopologicalOrderedGraph());
                log.debug("Checking Source Starvation");
                Instant now = Instant.now();

                createNewStarvationZeroSpendTransactions(now, topologicalOrderedGraph, transactionTrustChainTrustScoreMap);

                createNewGenesisZeroSpendTransactions();
            } catch (InterruptedException e) {
                log.info("Source Starvation Check was interrupted");
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error(BaseNodeMessages.EXCEPTION, e);
            }
        }
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

    private Hash findMajorChildHash(TransactionData parentTransactionData, Map<Hash, Double> transactionTrustChainTrustScoreMap) {
        double maxTrustChainTrustScore = 0;
        Hash majorChildHash = null;
        for (Hash child : parentTransactionData.getChildrenTransactionHashes()) {
            Double childTrustChainTrustScore = transactionTrustChainTrustScoreMap.get(child);
            if (childTrustChainTrustScore != null && childTrustChainTrustScore > maxTrustChainTrustScore) {
                maxTrustChainTrustScore = childTrustChainTrustScore;
                majorChildHash = child;
            }
        }
        return majorChildHash;
    }

    private void pairSourceToRoot(TransactionData sourceTransactionData, ConcurrentHashMap<Hash, TransactionData> sourceToRootMap,
                                  ConcurrentHashMap<TransactionData, TransactionData> rootSourcePairs, List<TransactionData> orphanedStarvationSources) {

        if (sourceToRootMap.get(sourceTransactionData.getHash()) != null) {
            rootSourcePairs.put(sourceToRootMap.get(sourceTransactionData.getHash()), sourceTransactionData);
        } else {
            if (!sourceTransactionData.getType().equals(TransactionType.ZeroSpend)) {
                rootSourcePairs.put(sourceTransactionData, sourceTransactionData);
            } else {
                orphanedStarvationSources.add(sourceTransactionData);
            }
        }
    }

    private boolean mapChildToParent(TransactionData parentTransactionData, ConcurrentHashMap<Hash, TransactionData> vertexMap,
                                     Hash majorChildHash) {
        boolean replaceRootIfNeeded = true;
        if (vertexMap.get(majorChildHash) != null) {
            TransactionData existingRoot = vertexMap.get(majorChildHash);
            if (!parentTransactionData.getAttachmentTime().isBefore(existingRoot.getAttachmentTime())) {
                replaceRootIfNeeded = false;
            }
        }
        return replaceRootIfNeeded;
    }

    private void mapPathUsingMajorChild(TransactionData parentTransactionData,
                                        ConcurrentHashMap<Hash, TransactionData> vertexMap,
                                        Hash majorChildHash) {
        TransactionData root = null;

        if (!TransactionType.ZeroSpend.equals(parentTransactionData.getType())) {
            root = vertexMap.getOrDefault(parentTransactionData.getHash(), parentTransactionData);
        } else if (vertexMap.containsKey(parentTransactionData.getHash())) {
            root = vertexMap.get(parentTransactionData.getHash());
        }
        if (root != null && mapChildToParent(parentTransactionData, vertexMap, majorChildHash)) {
            vertexMap.put(majorChildHash, root);
        }

    }

    private void pairSourcesWithRoots(LinkedList<TransactionData> topologicalOrderedGraph, ConcurrentHashMap<TransactionData, TransactionData> rootSourcePairs,
                                      List<TransactionData> orphanedStarvationSources, Map<Hash, Double> transactionTrustChainTrustScoreMap) {
        ConcurrentHashMap<Hash, TransactionData> vertexMap = new ConcurrentHashMap<>();

        for (int i = topologicalOrderedGraph.size() - 1; i >= 0; i--) {
            TransactionData parentTransactionData = topologicalOrderedGraph.get(i);
            Hash majorChildHash = findMajorChildHash(parentTransactionData, transactionTrustChainTrustScoreMap);

            if (majorChildHash != null) {
                mapPathUsingMajorChild(parentTransactionData, vertexMap, majorChildHash);
            } else {
                pairSourceToRoot(parentTransactionData, vertexMap, rootSourcePairs, orphanedStarvationSources);
            }
        }
    }

    private void createNewStarvationZeroSpendTransactions(Instant now, LinkedList<TransactionData> topologicalOrderedGraph, Map<Hash, Double> transactionTrustChainTrustScoreMap) {
        ConcurrentHashMap<TransactionData, TransactionData> rootSourcePairs = new ConcurrentHashMap<>();
        List<TransactionData> orphanedStarvationSources = new ArrayList<>();
        pairSourcesWithRoots(topologicalOrderedGraph, rootSourcePairs, orphanedStarvationSources, transactionTrustChainTrustScoreMap);

        Set<TransactionData> sourcesAttached = new HashSet<>();
        Set<TransactionData> zeroSpendSourcesAttached = new HashSet<>();
        List<TransactionData> newlyCreatedZeroSpends = new ArrayList<>();

        for (Map.Entry<TransactionData, TransactionData> rootSourcePair : rootSourcePairs.entrySet()) {
            TransactionData rootTransactionData = rootSourcePair.getKey();
            TransactionData sourceTransactionData = rootSourcePair.getValue();
            if (!sourcesAttached.contains(sourceTransactionData)) {
                long minimumWaitingTimeInMilliseconds = clusterHelper.getMinimumWaitTimeInMilliseconds(rootTransactionData);
                long actualWaitingTimeInMilliseconds = Duration.between(rootTransactionData.getAttachmentTime(), now).toMillis();
                log.debug("Waiting transaction: {}. Time without attachment: {}, Minimum wait time: {}", rootTransactionData.getHash(), millisecondsToMinutes(actualWaitingTimeInMilliseconds), millisecondsToMinutes(minimumWaitingTimeInMilliseconds));
                if (actualWaitingTimeInMilliseconds > minimumWaitingTimeInMilliseconds) {
                    createJointSourceStarvationZeroSpendTransaction(newlyCreatedZeroSpends, sourceTransactionData, sourcesAttached,
                            rootSourcePairs, orphanedStarvationSources, zeroSpendSourcesAttached);
                } else {
                    rootSourcePairs.remove(rootTransactionData);
                }
            }
        }

        for (TransactionData newZeroSpend : newlyCreatedZeroSpends) {
            transactionCreationService.attachAndSendZeroSpendTransaction(newZeroSpend);
        }
    }

    private void createJointSourceStarvationZeroSpendTransaction(List<TransactionData> newlyCreatedZeroSpends, TransactionData sourceTransactionData, Set<TransactionData> sourcesAttached,
                                                                 ConcurrentHashMap<TransactionData, TransactionData> rootSourcePairs, List<TransactionData> orphanedStarvationSources,
                                                                 Set<TransactionData> zeroSpendSourcesAttached) {
        TransactionData zeroSpendTransaction = transactionCreationService.createNewStarvationZeroSpendTransaction(sourceTransactionData);
        if (zeroSpendTransaction == null) {
            return;
        }
        newlyCreatedZeroSpends.add(zeroSpendTransaction);
        sourcesAttached.add(sourceTransactionData);
        List<TransactionData> possibleSources = clusterService.findSources(zeroSpendTransaction);
        possibleSources = possibleSources.stream().filter(p -> !p.getHash().equals(sourceTransactionData.getHash()) && !(p.getType().equals(TransactionType.ZeroSpend) && ZeroSpendTransactionType.GENESIS.toString().equals(p.getTransactionDescription()))).collect(Collectors.toList());
        for (TransactionData possibleOtherParent : rootSourcePairs.values()) {
            if (possibleSources.contains(possibleOtherParent) && !sourcesAttached.contains(possibleOtherParent)) {
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
