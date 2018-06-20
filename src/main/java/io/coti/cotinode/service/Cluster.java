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

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@Configurable
@Data
public class Cluster implements ICluster {

    private static Object locker = new Object();

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
        synchronized (locker) {
            // add to unTccConfirmedTransaction map
            addToUnTccConfirmedTransactionMap(transaction);

            //  add to TrustScoreToSourceList map
            addToTrustScoreToSourceListMap(transaction);
        }
    }

    private int getTotalNumberOfSources() {
        // Get num of all transactions in numberOfSources
        AtomicInteger numberOfSources = new AtomicInteger();
        trustScoreToSourceListMapping.forEach((score, transactions) -> {
            numberOfSources.addAndGet(transactions.size());
        });
        return numberOfSources.intValue();
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
        executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        hashToUnTccConfirmationTransactionsMapping = new ConcurrentHashMap<>();
        trustScoreToSourceListMapping = new ConcurrentHashMap<>();

        setUnTccConfirmedTransactions(notConfirmTransactions);
        setTrustScoreToSourceListMapping(hashToUnTccConfirmationTransactionsMapping);
        tccConfirmationService = new TccConfirmationService(); // @Autowired doesn't work in test ??
        sourceSelector = new SourceSelector();
        //forEach(transaction -> addNewTransaction(transaction));
        //  trustScoreConsensusProcess();
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

        synchronized (locker) {
            TransactionData transaction = null;
            if (hashToUnTccConfirmationTransactionsMapping.containsKey(hash)) {
                transaction = hashToUnTccConfirmationTransactionsMapping.get(hash);
                hashToUnTccConfirmationTransactionsMapping.remove(hash);
            }

            deleteTrustScoreToSourceListMapping(transaction);
        }
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

    }

    @Override
    public List<TransactionData> getAllSourceTransactions() {
        return hashToUnTccConfirmationTransactionsMapping.values().stream().
                filter(TransactionData::isSource).collect(Collectors.toList());
    }

    public List<TransactionData> getNewTransactions() {
        List<TransactionData> getNewTransactions = new Vector<>();

        //TODO: Get new transactions from the queue service

        getNewTransactions.parallelStream().forEach(transactionData -> addNewTransaction(transactionData));
        return getNewTransactions;
    }

    @Override
    public boolean addNewTransaction(TransactionData transaction) {

        try {
            transaction.setProcessStartTime(new Date());
            log.info("{} Starting process of attaching transaction with hash: {}: trustScore: {}", Instant.now(), transaction.getHash(), transaction.getSenderTrustScore());
            // TODO: Get The transaction trust score from trust score node.

            final ConcurrentHashMap<Integer, List<TransactionData>> localThreadTrustScoreToSourceListMapping =
                    new ConcurrentHashMap<>(trustScoreToSourceListMapping);


//        for (final List<TransactionData> finalSelectedSourcesForAttachment = new Vector<TransactionData>(selectedSourcesForAttachment);
//             finalSelectedSourcesForAttachment.size() == 0 &&  localThreadTustScoreToSourceListMapping.size() > 0 ; ) {
            AtomicBoolean executed = new AtomicBoolean();

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    do {
                        List<TransactionData> selectedSourcesForAttachment = null;
                        executed.set(true);
                        int localTrustScoreToSourceListMappingSum = getTotalNumberOfSources();
                        log.info("{} num of aources: {}", Instant.now(), localTrustScoreToSourceListMappingSum);
                        if (localTrustScoreToSourceListMappingSum > 0) {

                            // Selection of sources
                            selectedSourcesForAttachment = sourceSelector.selectSourcesForAttachment(localThreadTrustScoreToSourceListMapping,
                                    transaction.getSenderTrustScore(),
                                    transaction.getCreateTime(),
                                    10, // TODO: get value from config file and/or dynamic
                                    20); // TODO:  get value from config file and/or dynamic
                            if (selectedSourcesForAttachment.size() == 0) {
                                executed.set(false);
                                log.info("{} in attachment process of transaction with hash: {}: whe have source: {}, waiting for more transactions!!", Instant.now(), transaction.getHash());
                            } else {
                                log.info("{} in attachment process of transaction with hash: {}: whe have source: {}, continue to POW!!", Instant.now(), transaction.getHash());
                            }
                        } else {
                            log.info("{} in attachment process of transaction with hash: {}: , there is no other transactions in cluster. No need for attachment", Instant.now(), transaction.getHash());
                        }

                        if (executed.get() && PowProcess(transaction)) {

                            if (localTrustScoreToSourceListMappingSum > 0) {

                                // Attache sources
                                attachmentProcess(localThreadTrustScoreToSourceListMapping, selectedSourcesForAttachment, transaction);
                            }
                            // updating transaction collections with the new transaction
                            addNewTransactionToAllCollections(transaction);

                            transaction.setProcessEndTime(new Date());
                            log.info("{} Ending process of attaching transaction with hash: {} trustScore: {}  LeftParentHash: {}  RightParentHash: {}",
                                    Instant.now(),
                                    transaction.getHash(),
                                    transaction.getSenderTrustScore(),
                                    transaction.getLeftParent(),
                                    transaction.getRightParent());

                        }
                        try {
                            TimeUnit.SECONDS.sleep(3);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } while (!executed.get());
                }

            });

        } catch (Exception e) {
            log.error(e.toString());
        }

        return true;
    }

    private boolean PowProcess(TransactionData transaction) {
        log.info("{} in attachment process of transaction with hash: {}: , starting POW", Instant.now(), transaction.getHash());
        transaction.setPowStartTime(new Date());
        // TODO : POW
        transaction.setPowEndTime(new Date());
        log.info("{} in attachment process of transaction with hash: {}: , ending POW", Instant.now(), transaction.getHash());
        return true;
    }

    private void attachmentProcess(ConcurrentHashMap<Integer, List<TransactionData>> localThreadTrustScoreToSourceListMapping,
                                   List<TransactionData> selectedSourcesForAttachment,
                                   TransactionData transaction) {
        if (localThreadTrustScoreToSourceListMapping.size() > 1) {

            for (TransactionData sourceTransaction : selectedSourcesForAttachment) {
                log.info("{} in attachment process of transaction with hash: {}: whe have source: {}", Instant.now(), transaction.getHash(), sourceTransaction.getHash());
                attachToSource(transaction, sourceTransaction);
            }
            // TODO: Validate the sources.

        }

    }

    @Override
    public void attachToSource(TransactionData newTransaction, TransactionData source) {
        if (hashToUnTccConfirmationTransactionsMapping.get(source.getKey()) == null) {
            log.error("Cannot find source:" + source);
        }
        newTransaction.setAttachmentTime(new Date());

        synchronized (locker) {
            newTransaction.attachToSource(source);
            deleteTrustScoreToSourceListMapping(source);
        }
    }

    @Override
    public void trustScoreConsensusProcess() {
        log.info("###Start Processing with Thread id: " + Thread.currentThread().getId());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    tccConfirmationService.init(hashToUnTccConfirmationTransactionsMapping);
                    tccConfirmationService.topologicalSorting();
                    List<Hash> transactionConsensusConfirmed = tccConfirmationService.setTransactionConsensus();

                    // Update TransactionService & BalanceService
                    for (Hash hash : transactionConsensusConfirmed) {
                        log.info(" {} transaction with hash: {}: is confirmed with trustScore: {} ans totalTrustScore: {}",
                                Instant.now(),
                                hashToUnTccConfirmationTransactionsMapping.get(hash).getHash(),
                                hashToUnTccConfirmationTransactionsMapping.get(hash).getSenderTrustScore(),
                                hashToUnTccConfirmationTransactionsMapping.get(hash).getTotalTrustScore());
                        deleteTransactionFromHashToUnTccConfirmedTransactionsMapping(hash);
                        //balanceService.
                    }
                    try {
                        TimeUnit.SECONDS.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }
}

