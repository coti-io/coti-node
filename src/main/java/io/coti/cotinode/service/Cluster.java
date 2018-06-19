package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.service.interfaces.ISourceSelector;

import io.coti.cotinode.service.interfaces.ICluster;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@Configurable
@Data
public class Cluster implements ICluster {

    @Autowired
    private ISourceSelector sourceSelector;

    @Autowired
    private TccConfirmationService tccConfirmationService;

    @Autowired
    private BalanceService balanceService;

    private ConcurrentHashMap<Hash, TransactionData> hashToUnTccConfirmationTransactionsMapping;
    private ConcurrentHashMap<Integer, List<TransactionData>> trustScoreToSourceListMapping;
    private Executor executor;

    private void addNewTransactionToAllCollections(TransactionData transaction) {
        // add to unTccConfirmedTransaction map
        addToUnTccConfirmedTransactionMap(transaction);

        //  add to TrustScoreToSourceList map
        addToTrustScoreToSourceListMap(transaction);
    }

    private void setUnTccConfirmedTransactions(List<TransactionData> allTransactions) {
        this.hashToUnTccConfirmationTransactionsMapping.
                putAll(allTransactions.stream().
                        filter(x -> !x.isTransactionConsensus()).
                        collect(Collectors.
                                toMap(TransactionData::getHash, Function.identity())));
    }

    private void setTrustScoreToSourceListMapping(ConcurrentHashMap<Hash, TransactionData> hashToUnconfirmedTransactionsMapping) {
        this.trustScoreToSourceListMapping = new ConcurrentHashMap<>();
        for (int i = 1; i <= 100; i++) {
            trustScoreToSourceListMapping.put(i, new Vector<TransactionData>());
        }

        for (TransactionData transaction : hashToUnconfirmedTransactionsMapping.values()) {
            if (transaction.isSource() && transaction.getSenderTrustScore() >= 1 && transaction.getSenderTrustScore() <= 100) {
                this.trustScoreToSourceListMapping.get(transaction.getSenderTrustScore()).add(transaction);
            }
        }
    }

    @Override
    public void initCluster(List<TransactionData> notConfirmTransactions) {
        executor = Executors.newCachedThreadPool();
        hashToUnTccConfirmationTransactionsMapping = new ConcurrentHashMap<>();
        trustScoreToSourceListMapping = new ConcurrentHashMap<>();

        setUnTccConfirmedTransactions(notConfirmTransactions);
        setTrustScoreToSourceListMapping(hashToUnTccConfirmationTransactionsMapping);
        tccConfirmationService = new TccConfirmationService(); // @Autowired doesn't work in test ??
        trustScoreConsensusProcess();
    }

    @Override
    public void addToUnTccConfirmedTransactionMap(TransactionData transaction) {
        hashToUnTccConfirmationTransactionsMapping.put(transaction.getHash(), transaction);
        // TODO use the TransactionService
    }

    @Override
    public void addToTrustScoreToSourceListMap(TransactionData transaction) {

        if (transaction.isSource() && transaction.getSenderTrustScore() >= 1 && transaction.getSenderTrustScore() <= 100) {
            this.trustScoreToSourceListMapping.get(transaction.getSenderTrustScore()).add(transaction);
        }
        // TODO use the TransactionService
    }

    @Override
    public void deleteTransactionFromHashToUnTccConfirmedTransactionsMapping(Hash hash) {
        TransactionData transaction = null;
        if (hashToUnTccConfirmationTransactionsMapping.containsKey(hash)) {
            transaction = hashToUnTccConfirmationTransactionsMapping.get(hash);
            hashToUnTccConfirmationTransactionsMapping.remove(hash);
        }

        //persistenceProvider.deleteTransaction(hash);
        // TODO: replace with TransactionService

        deleteTrustScoreToSourceListMapping(transaction);
    }

    @Override
    public void deleteTrustScoreToSourceListMapping(TransactionData transaction) {
        if (trustScoreToSourceListMapping.containsKey(transaction.getSenderTrustScore())) {
            trustScoreToSourceListMapping.get(transaction.getSenderTrustScore()).remove(transaction);
        } else {
            for (List<TransactionData> transactionList : trustScoreToSourceListMapping.values()) {
                if (transactionList.contains(transaction)) {
                    transactionList.remove(transaction);
                }
            }
        }

        //persistenceProvider.deleteTransaction(hash);
        // TODO: replace with TransactionService
    }

    @Override
    public List<TransactionData> getAllSourceTransactions() {
        return hashToUnTccConfirmationTransactionsMapping.values().stream().
                filter(TransactionData::isSource).collect(Collectors.toList());
    }

    public List<TransactionData> getNewTransactions() {
        List<TransactionData> getNewTransactions = new Vector<>();

        //TODO: Get new transactions from the queue service

        return getNewTransactions;
    }

    @Override
    public boolean addNewTransaction(TransactionData transaction) {
        transaction.setProcessStartTime(new Date());

        // TODO: Get The transaction trust score from trust score node.

        List<TransactionData> selectedSourcesForAttachment = null;
        ConcurrentHashMap<Integer, List<TransactionData>> localThreadTustScoreToSourceListMapping =
                new ConcurrentHashMap<>(trustScoreToSourceListMapping);
        if (localThreadTustScoreToSourceListMapping.size() > 1) {

            // Selection of sources
            selectedSourcesForAttachment = sourceSelector.selectSourcesForAttachment(localThreadTustScoreToSourceListMapping,
                    transaction.getSenderTrustScore(),
                    transaction.getAttachmentTime(),
                    5, // TODO: get value from config file and/or dynamic
                    10); // TODO:  get value from config file and/or dynamic
        }

        final List<TransactionData> finalSelectedSourcesForAttachment = new Vector<TransactionData>(selectedSourcesForAttachment);

        executor.execute(() -> {
            // if (isFromPropagation) {
            transaction.setPowStartTime(new Date());
            // TODO : POW
            transaction.setPowEndTime(new Date());
            //  }

            // Attache sources
            if (localThreadTustScoreToSourceListMapping.size() > 1) {
                if (finalSelectedSourcesForAttachment.size() == 0) {
                    // TODO: wait
                }

                for (TransactionData sourceTransaction : finalSelectedSourcesForAttachment) {
                    attachToSource(transaction, sourceTransaction);
                }
            }

            // updating transaction collections with the new transaction
            addNewTransactionToAllCollections(transaction);

            // Update the total trust score of the parents
            //updateParentsTotalSumScore(transaction, 0, transaction.getTrustChainTransactionHashes());

            transaction.setProcessEndTime(new Date());
        });
        // TODO: Validate the sources.

        return true;
    }

    @Override
    public void attachToSource(TransactionData newTransaction, TransactionData source) {
        if (hashToUnTccConfirmationTransactionsMapping.get(source.getKey()) == null) {
            log.error("Cannot find source:" + source);
        }
        newTransaction.attachToSource(source);
        newTransaction.setAttachmentTime(new Date());
        deleteTrustScoreToSourceListMapping(source);
    }

    @Override
    public void trustScoreConsensusProcess() {
        log.info("###Start Processing with Thread id: " + Thread.currentThread().getId());

        while (true) {
            executor.execute(() -> {

                tccConfirmationService.init(hashToUnTccConfirmationTransactionsMapping);
                tccConfirmationService.topologicalSorting();
                List<Hash> transactionConsensusConfirmed = tccConfirmationService.setTransactionConsensus();

                // Update TransactionService & BalanceService
                for (Hash hash : transactionConsensusConfirmed) {
                    deleteTransactionFromHashToUnTccConfirmedTransactionsMapping(hash);
                    //balanceService.
                }


            });
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

