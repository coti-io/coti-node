package io.coti.basenode.services;

import com.google.common.collect.Sets;
import io.coti.basenode.data.*;
import io.coti.basenode.services.interfaces.IClusterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static io.coti.basenode.services.BaseNodeServiceManager.*;

@Slf4j
@Service
public class ClusterService implements IClusterService {

    private static final int TCC_CONFIRMATION_INTERVAL = 3000;
    private final AtomicLong totalSources = new AtomicLong(0);
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private ArrayList<HashSet<Hash>> sourceSetsByTrustScore;
    private HashMap<Hash, TransactionData> sourceMap;
    private ConcurrentHashMap<Hash, TransactionData> trustChainConfirmationCluster;
    private Thread trustChainConfirmedTransactionsThread;
    private boolean initialConfirmation = true;
    private BlockingQueue<TrustChainConfirmationResult> trustChainConfirmationResults;

    public void init() {
        trustChainConfirmationCluster = new ConcurrentHashMap<>();
        sourceSetsByTrustScore = new ArrayList<>();
        sourceMap = new HashMap<>();
        for (int i = 0; i <= 100; i++) {
            sourceSetsByTrustScore.add(Sets.newHashSet());
        }
        trustChainConfirmationResults = new ArrayBlockingQueue<>(11);
        trustChainConfirmedTransactionsThread = new Thread(this::checkForTrustChainConfirmedTransaction, "CLUSTER-SERVICE TCC CHECK");
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public void addExistingTransactionOnInit(TransactionData transactionData) {
        removeTransactionParentsFromSources(transactionData);
        if (!transactionData.isTrustChainConsensus()) {
            addTransactionToTCCClusterAndToSources(transactionData);
        }
    }

    @Override
    public void addMissingTransactionOnInit(TransactionData transactionData, Set<Hash> trustChainUnconfirmedExistingTransactionHashes) {
        updateParentsByMissingTransaction(transactionData, trustChainUnconfirmedExistingTransactionHashes);
        if (!transactionData.isTrustChainConsensus()) {
            addTransactionToTCCClusterAndToSources(transactionData);
        } else if (trustChainUnconfirmedExistingTransactionHashes.remove(transactionData.getHash())) {
            removeTransactionFromTCCClusterAndSources(transactionData);
        }
    }

    private void updateParentsByMissingTransaction(TransactionData transactionData, Set<Hash> trustChainUnconfirmedExistingTransactionHashes) {
        if (transactionData.getLeftParentHash() != null && trustChainUnconfirmedExistingTransactionHashes.contains(transactionData.getLeftParentHash())) {
            updateSingleParent(transactionData, transactionData.getLeftParentHash());
        }
        if (transactionData.getRightParentHash() != null && trustChainUnconfirmedExistingTransactionHashes.contains(transactionData.getRightParentHash())) {
            updateSingleParent(transactionData, transactionData.getRightParentHash());
        }
        removeTransactionParentsFromSources(transactionData);
    }

    @Override
    public void startToCheckTrustChainConfirmation() {
        trustChainConfirmedTransactionsThread.start();
    }

    public void checkForTrustChainConfirmedTransaction() {
        while (!Thread.currentThread().isInterrupted()) {
            LinkedList<TransactionData> topologicalOrderedGraph;
            Map<Hash, Double> transactionTrustChainTrustScoreMap;

            trustChainConfirmationService.init(trustChainConfirmationCluster);
            topologicalOrderedGraph = trustChainConfirmationService.getTopologicalOrderedGraph();
            List<TccInfo> transactionConsensusConfirmed = trustChainConfirmationService.getTrustChainConfirmedTransactions();
            transactionTrustChainTrustScoreMap = trustChainConfirmationService.getTransactionTrustChainTrustScoreMap();
            if (initialConfirmation) {
                if (transactionConsensusConfirmed.isEmpty()) {
                    synchronized (confirmationService.getInitialTccConfirmationLock()) {
                        confirmationService.getInitialTccConfirmationFinished().set(true);
                        confirmationService.getInitialTccConfirmationLock().notifyAll();
                    }
                } else {
                    confirmationService.getInitialConfirmationStarted().set(true);
                }
            }
            transactionConsensusConfirmed.forEach(tccInfo -> {
                TransactionData transactionData = trustChainConfirmationCluster.remove(tccInfo.getHash());
                transactionTrustChainTrustScoreMap.remove(tccInfo.getHash());
                if (transactionData != null) {
                    topologicalOrderedGraph.remove(transactionData);
                }
                confirmationService.setTccToTrue(tccInfo);
                log.debug("TCC has been reached for transaction {}!!", tccInfo.getHash());
            });
            initialConfirmation = false;

            trustChainConfirmationResults.clear();
            trustChainConfirmationResults.add(new TrustChainConfirmationResult(transactionTrustChainTrustScoreMap, topologicalOrderedGraph));
            try {
                Thread.sleep(TCC_CONFIRMATION_INTERVAL);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void attachToCluster(TransactionData transactionData) {
        updateParents(transactionData);

        addTransactionToTCCClusterAndToSources(transactionData);
    }

    @Override
    public void detachFromCluster(TransactionData transactionData) {
        removeTransactionFromSources(transactionData.getHash());

        updateParentsToDetachChild(transactionData);
    }

    private void updateParentsToDetachChild(TransactionData transactionData) {
        removeChildFromParent(transactionData, transactionData.getLeftParentHash());
        removeChildFromParent(transactionData, transactionData.getRightParentHash());
        restoreTransactionParentsAsSources(transactionData);
    }

    private void removeChildFromParent(TransactionData transactionData, Hash parentHash) {
        if (parentHash != null) {
            transactions.lockAndGetByHash(parentHash, parentTransactionData -> {
                if (parentTransactionData != null) {
                    if (!parentTransactionData.removeFromChildrenTransactions(transactionData.getHash())) {
                        log.error("Failed to remove child: {} from parent: {}", transactionData.getHash(), parentHash);
                    }
                    if (trustChainConfirmationCluster.containsKey(parentTransactionData.getHash())) {
                        trustChainConfirmationCluster.put(parentTransactionData.getHash(), parentTransactionData);
                    } else {
                        log.error("Failed to update in trustChainConfirmationCluster parent transaction: {}", parentTransactionData.getHash());
                    }
                    transactions.put(parentTransactionData);
                }
            });
        }
    }

    private void restoreTransactionParentsAsSources(TransactionData transactionData) {
        if (transactionData.getLeftParentHash() != null) {
            restoreTransactionAsSource(transactionData.getLeftParentHash());
        }
        if (transactionData.getRightParentHash() != null) {
            restoreTransactionAsSource(transactionData.getRightParentHash());
        }
    }

    private void restoreTransactionAsSource(Hash transactionHash) {
        TransactionData transactionData = transactions.getByHash(transactionHash);
        if (transactionData != null) {
            addNewSourceTransactionToSources(transactionData);
        } else {
            log.error("Failed to find parent Transaction with hash:{}", transactionHash);
        }
    }

    private void updateParents(TransactionData transactionData) {
        updateSingleParent(transactionData, transactionData.getLeftParentHash());
        updateSingleParent(transactionData, transactionData.getRightParentHash());
        removeTransactionParentsFromSources(transactionData);
    }

    private void updateSingleParent(TransactionData transactionData, Hash parentHash) {
        if (parentHash != null) {
            transactions.lockAndGetByHash(parentHash, parentTransactionData -> {
                if (parentTransactionData != null && !parentTransactionData.getChildrenTransactionHashes().contains(transactionData.getHash())) {
                    parentTransactionData.addToChildrenTransactions(transactionData.getHash());
                    if (trustChainConfirmationCluster.containsKey(parentTransactionData.getHash())) {
                        trustChainConfirmationCluster.put(parentTransactionData.getHash(), parentTransactionData);
                    }
                    transactions.put(parentTransactionData);
                }
            });
        }
    }

    private void removeTransactionParentsFromSources(TransactionData transactionData) {
        if (transactionData.getLeftParentHash() != null) {
            removeTransactionFromSources(transactionData.getLeftParentHash());
        }
        if (transactionData.getRightParentHash() != null) {
            removeTransactionFromSources(transactionData.getRightParentHash());
        }
    }

    private void removeTransactionFromSources(Hash transactionHash) {
        try {
            readWriteLock.writeLock().lock();
            TransactionData transactionData = sourceMap.remove(transactionHash);
            if (transactionData != null) {
                sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).remove(transactionHash);
                totalSources.decrementAndGet();
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    private void addNewSourceTransactionToSources(TransactionData transactionData) {
        Hash transactionHash = transactionData.getHash();
        try {
            readWriteLock.writeLock().lock();
            if (transactionData.isSource() && sourceMap.put(transactionHash, transactionData) == null) {
                sourceSetsByTrustScore.get(transactionData.getRoundedSenderTrustScore()).add(transactionHash);
                totalSources.incrementAndGet();
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }

        log.debug("Added New Transaction with hash:{}", transactionHash);
    }

    @Override
    public void addTransactionToTrustChainConfirmationCluster(TransactionData transactionData) {
        if (!nodeEventService.eventHappened(Event.TRUST_SCORE_CONSENSUS) || nodeTransactionHelper.isDspConfirmed(transactionData))
            updateTransactionOnTrustChainConfirmationCluster(transactionData);
    }

    public void removeTransactionFromTrustChainConfirmationCluster(TransactionData transactionData) {
        trustChainConfirmationCluster.remove(transactionData.getHash());
    }

    private void addTransactionToTCCClusterAndToSources(TransactionData transactionData) {
        addTransactionToTrustChainConfirmationCluster(transactionData);
        addNewSourceTransactionToSources(transactionData);
    }

    private void removeTransactionFromTCCClusterAndSources(TransactionData transactionData) {
        removeTransactionFromTrustChainConfirmationCluster(transactionData);
        removeTransactionFromSources(transactionData.getHash());
    }

    @Override
    public void updateTransactionOnTrustChainConfirmationCluster(TransactionData transactionData) {
        final TransactionData oldTransactionData = trustChainConfirmationCluster.put(transactionData.getHash(), transactionData);
        if (oldTransactionData == null) {
            log.debug("Updated cluster with a new Transaction with hash:{}", transactionData.getHash());
        }
    }

    @Override
    public List<TransactionData> findSources(TransactionData transactionData) {
        List<Set<Hash>> trustScoreToTransactionMappingSnapshot =
                Collections.unmodifiableList(sourceSetsByTrustScore);
        Map<Hash, TransactionData> sourceMapSnapshot = Collections.unmodifiableMap(sourceMap);
        return sourceSelector.selectSourcesForAttachment(
                trustScoreToTransactionMappingSnapshot,
                sourceMapSnapshot,
                transactionData.getSenderTrustScore(), readWriteLock);
    }

    @Override
    public void selectSources(TransactionData transactionData) {
        List<TransactionData> selectedSourcesForAttachment = findSources(transactionData);
        if (selectedSourcesForAttachment.isEmpty()) {
            return;
        }

        transactionData.setLeftParentHash(selectedSourcesForAttachment.get(0).getHash());
        if (selectedSourcesForAttachment.size() > 1) {
            transactionData.setRightParentHash(selectedSourcesForAttachment.get(1).getHash());
        }

        List<Hash> selectedSourceHashes = selectedSourcesForAttachment.stream().map(TransactionData::getHash).collect(Collectors.toList());
        log.debug("For transaction with hash: {} we found the following sources: {}", transactionData.getHash(), selectedSourceHashes);
    }

    @Override
    public long getTotalSources() {
        return totalSources.get();
    }

    @Override
    public Set<Hash> getTrustChainConfirmationTransactionHashes() {
        return new HashSet<>(trustChainConfirmationCluster.keySet());
    }

    @Override
    public ConcurrentHashMap<Hash, TransactionData> getCopyTrustChainConfirmationCluster() {
        return SerializationUtils.clone(trustChainConfirmationCluster);
    }

    @Override
    public ArrayList<HashSet<Hash>> getSourceSetsByTrustScore() {
        return SerializationUtils.clone(sourceSetsByTrustScore);
    }

    @Override
    public double getRuntimeTrustChainTrustScore(Hash transactionHash) {
        return Optional.ofNullable(trustChainConfirmationCluster.get(transactionHash)).map(TransactionData::getTrustChainTrustScore).orElse((double) 0);
    }

    @Override
    public BlockingQueue<TrustChainConfirmationResult> getTrustChainConfirmationResults() {
        return trustChainConfirmationResults;
    }
}
