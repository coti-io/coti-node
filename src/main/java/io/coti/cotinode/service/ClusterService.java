package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.model.Transactions;
import io.coti.cotinode.service.interfaces.ISourceSelector;

import io.coti.cotinode.service.interfaces.ICluster;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;
import io.coti.cotinode.data.TransactionData;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClusterService implements ICluster {

    private static Object locker = new Object();

    @Autowired
    QueueService queueService;

    @Autowired
    private Transactions dbTransactions;

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
            transactions.forEach(transaction -> log.info("sorce with hash:{}", transaction.getHash()));

        });

        return numberOfSources.intValue();
    }

    private void setUnTccConfirmedTransactions(List<TransactionData> notConfirmTransactions) {

        this.hashToUnTccConfirmationTransactionsMapping.
                putAll(notConfirmTransactions.stream().
                        //filter(x -> !x.isTransactionConsensus()).
                                collect(Collectors.
                                toMap(TransactionData::getHash, Function.identity())));
    }

    private void setTrustScoreToSourceListMapping(ConcurrentHashMap<Hash, TransactionData> hashToUnconfirmedTransactionsMapping) {
        log.info("starting function setTrustScoreToSourceListMapping");
        this.trustScoreToSourceListMapping = new ConcurrentHashMap<>();
        for (int i = 1; i <= 100; i++) {
            trustScoreToSourceListMapping.put(i, new Vector<TransactionData>());
        }

        for (TransactionData transaction : hashToUnconfirmedTransactionsMapping.values()) {
            addToTrustScoreToSourceListMap(transaction);
            log.info("calling addToTrustScoreToSourceListMap with transaction hash:{}", transaction);
        }
        log.info("starting function setTrustScoreToSourceListMapping");
    }

    private List<TransactionData> getTransactionsByHashFromDb(List<Hash> notConfirmTransactions) {
        List<TransactionData> transactions = new Vector<>();
        notConfirmTransactions.forEach(
                hash -> transactions.add(dbTransactions.getByHash(hash))
        );
        return transactions;
    }


    private void addToUnTccConfirmedTransactionMap(TransactionData transaction) {
        hashToUnTccConfirmationTransactionsMapping.put(transaction.getHash(), transaction);
        // TODO use the TransactionService
    }

    private void addToTrustScoreToSourceListMap(TransactionData transaction) {

        synchronized (locker) {
            if (transaction.isSource() && transaction.getSenderTrustScore() >= 1 && transaction.getSenderTrustScore() <= 100) {
                this.trustScoreToSourceListMapping.get(transaction.getSenderTrustScore()).add(transaction);
                log.info("adding source to trustScoreToSourceListMapping with hash:{}", transaction.getHash());
            }
        }
        // TODO use the TransactionService
    }

    private void deleteTransactionFromHashToUnTccConfirmedTransactionsMapping(Hash hash) {

        synchronized (locker) {
            TransactionData transaction = null;
            if (hashToUnTccConfirmationTransactionsMapping.containsKey(hash)) {
                transaction = hashToUnTccConfirmationTransactionsMapping.get(hash);
                hashToUnTccConfirmationTransactionsMapping.remove(hash);
            }

            deleteTrustScoreToSourceListMapping(transaction);
        }
    }

    private void deleteTrustScoreToSourceListMapping(TransactionData transaction) {
        if (transaction != null && trustScoreToSourceListMapping.containsKey(transaction.getSenderTrustScore())) {
            trustScoreToSourceListMapping.get(transaction.getSenderTrustScore()).remove(transaction);
        }
//        else {
//            for (List<TransactionData> transactionList : trustScoreToSourceListMapping.values()) {
//                if (transactionList.contains(transaction)) {
//                    transactionList.remove(transaction);
//                }
//            }
//        }

    }

    private List<TransactionData> getAllSourceTransactions() {
        return hashToUnTccConfirmationTransactionsMapping.values().stream().
                filter(TransactionData::isSource).collect(Collectors.toList());
    }


    private void attachmentProcess(TransactionData attachedTransactionFromDb) {

        TransactionData transactionInUnconfirmedMap = hashToUnTccConfirmationTransactionsMapping.get(attachedTransactionFromDb.getHash());

     //   transactionInUnconfirmedMap.attachToSources(attachedTransactionFromDb);
        deleteTrustScoreToSourceListMapping(dbTransactions.getByHash(attachedTransactionFromDb.getLeftParentHash()));
        deleteTrustScoreToSourceListMapping(dbTransactions.getByHash(attachedTransactionFromDb.getRightParentHash()));
        addNewTransactionToAllCollections(attachedTransactionFromDb);
    }

    private void trustScoreConsensusProcess() {
        log.info("###Start Processing with Thread id: " + Thread.currentThread().getId());

        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    handleUnconfirmedFromQueueTransactions();
                    tccConfirmationService.init(hashToUnTccConfirmationTransactionsMapping);
                    tccConfirmationService.topologicalSorting();
                    List<Hash> transactionConsensusConfirmed = tccConfirmationService.setTransactionConsensus();

                    for (Hash hash : transactionConsensusConfirmed) {
                        log.info("transaction with hash:{}: is confirmed with trustScore: {} ans totalTrustScore: {} !!!",
                                hashToUnTccConfirmationTransactionsMapping.get(hash).getHash(),
                                hashToUnTccConfirmationTransactionsMapping.get(hash).getSenderTrustScore(),
                                hashToUnTccConfirmationTransactionsMapping.get(hash).getTotalTrustScore());
                        deleteTransactionFromHashToUnTccConfirmedTransactionsMapping(hash);
                        queueService.addToUpdateBalanceQueue(hash);

                    }
                    try {
                        TimeUnit.SECONDS.sleep(15);
                    } catch (InterruptedException e) {
                        log.error(e.toString());
                    }
                }
            }
        });
    }

    @Override
    public void initCluster(List<Hash> notConfirmTransactions) {

        try {
            executor = Executors.newCachedThreadPool();
            hashToUnTccConfirmationTransactionsMapping = new ConcurrentHashMap<>();
            trustScoreToSourceListMapping = new ConcurrentHashMap<>();

            dbTransactions.init();
            setUnTccConfirmedTransactions(getTransactionsByHashFromDb(notConfirmTransactions));
            setTrustScoreToSourceListMapping(hashToUnTccConfirmationTransactionsMapping);

            trustScoreConsensusProcess();
            log.info("end initCluster function");
        } catch (Exception e) {
            log.error("Error in initCluster", e);
        }
    }


    @Override
    public boolean addNewTransaction(TransactionData transactionFromDb) {
        boolean thereAreSources = true;
        try {
            transactionFromDb.setProcessStartTime(new Date());
            log.info("Starting process of attaching transactionFromDb with hash:{}: trustScore: {}", transactionFromDb.getHash(), transactionFromDb.getSenderTrustScore());

            // TODO: Get The transactionFromDb trust score from trust score node.

            ConcurrentHashMap<Integer, List<TransactionData>> localThreadTrustScoreToSourceListMapping = null;


            List<TransactionData> selectedSourcesForAttachment = null;
            int localTrustScoreToSourceListMappingSum;
            synchronized (locker) {
                localThreadTrustScoreToSourceListMapping = new ConcurrentHashMap<>(trustScoreToSourceListMapping);
                localTrustScoreToSourceListMappingSum = getTotalNumberOfSources();
            }
            log.info("num of sources: {}", localTrustScoreToSourceListMappingSum);
            if (localTrustScoreToSourceListMappingSum > 0) {

                // Selection of sources
                selectedSourcesForAttachment = sourceSelector.selectSourcesForAttachment(localThreadTrustScoreToSourceListMapping,
                        transactionFromDb.getSenderTrustScore(),
                        new Date(),
                        10, // TODO: get value from config file and/or dynamic
                        20); // TODO:  get value from config file and/or dynamic
                if (selectedSourcesForAttachment.size() == 0) {
                    log.info("in attachment process of transactionFromDb with hash:{}: whe have source:, waiting for more transactions", transactionFromDb.getHash());
                } else if (selectedSourcesForAttachment.size() == 1) {
                    log.info("in attachment process of transactionFromDb with hash:{}: whe have source: {}", transactionFromDb.getHash());
                    transactionFromDb.setLeftParentHash(selectedSourcesForAttachment.get(0).getHash());
                } else if (selectedSourcesForAttachment.size() == 2) {
                    log.info("in attachment process of transactionFromDb with hash:{}: whe have source: {}", transactionFromDb.getHash());
                    transactionFromDb.setLeftParentHash(selectedSourcesForAttachment.get(0).getHash());
                    transactionFromDb.setLeftParentHash(selectedSourcesForAttachment.get(1).getHash());
                }
            } else {
                thereAreSources = false;
                log.info("in attachment process of transactionFromDb with hash:{}: , there is no other transactions in cluster. No need for attachment", transactionFromDb.getHash());
            }

            // Test only!!
            if (!thereAreSources || selectedSourcesForAttachment.size() > 0) {
                dbTransactions.put(transactionFromDb);
                queueService.addToTccQueue(transactionFromDb.getHash());
            }
            /////////////

        } catch (Exception e) {
            log.error("error in addNewTransaction", e);
        }
        log.info("Ending process of attaching transactionFromDb with hash:{} trustScore: {}  LeftParentHash:{}  RightParentHash:{} !!!",
                transactionFromDb.getHash(),
                transactionFromDb.getSenderTrustScore(),
                transactionFromDb.getLeftParentHash(),
                transactionFromDb.getRightParentHash());

        return thereAreSources;
    }


    // This function is for test purpose only
    public void initClusterFromTransactionList(List<TransactionData> notConfirmTransactions) {

        try {
            executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
            hashToUnTccConfirmationTransactionsMapping = new ConcurrentHashMap<>();
            trustScoreToSourceListMapping = new ConcurrentHashMap<>();

            setUnTccConfirmedTransactions(notConfirmTransactions);
            setTrustScoreToSourceListMapping(hashToUnTccConfirmationTransactionsMapping);
            tccConfirmationService = new TccConfirmationService(); // @Autowired doesn't work in test ??
            sourceSelector = new SourceSelector();
            trustScoreConsensusProcess();
            log.info("end initCluster function");
        } catch (Exception e) {
            log.error("error in initClusterFromTransactionLis",e);
        }
    }

    private void handleUnconfirmedFromQueueTransactions() {
        ConcurrentLinkedQueue<Hash> UnconfirmedTransactionsHashFromQueue = queueService.getTccQueue();

        for (Hash hash : UnconfirmedTransactionsHashFromQueue) {
            TransactionData transaction = dbTransactions.getByHash(hash);
            attachmentProcess(transaction);
        }
        queueService.removeTccQueue();
    }

}

