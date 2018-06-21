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
import io.coti.cotinode.data.TransactionData;
import org.springframework.stereotype.Service;

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
public class ClusterService implements ICluster {

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

    @Override
    public void initCluster(List<TransactionData> notConfirmTransactions) {
        executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        hashToUnTccConfirmationTransactionsMapping = new ConcurrentHashMap<>();
        trustScoreToSourceListMapping = new ConcurrentHashMap<>();

        setUnTccConfirmedTransactions(notConfirmTransactions);
        setTrustScoreToSourceListMapping(hashToUnTccConfirmationTransactionsMapping);
        tccConfirmationService = new TccConfirmationService(); // @Autowired doesn't work in test ??
        sourceSelector = new SourceSelector();
        trustScoreConsensusProcess();
        log.info("end initCluster function");
    }

    @Override
    public void addToUnTccConfirmedTransactionMap(TransactionData transaction) {
        hashToUnTccConfirmationTransactionsMapping.put(transaction.getHash(), transaction);
        // TODO use the TransactionService
    }

    @Override
    public void addToTrustScoreToSourceListMap(TransactionData transaction) {

        synchronized (locker) {
            if (transaction.isSource() && transaction.getSenderTrustScore() >= 1 && transaction.getSenderTrustScore() <= 100) {
                this.trustScoreToSourceListMapping.get(transaction.getSenderTrustScore()).add(transaction);
                log.info("adding source to trustScoreToSourceListMapping with hash:{}", transaction.getHash());
            }
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
        }
//        else {
//            for (List<TransactionData> transactionList : trustScoreToSourceListMapping.values()) {
//                if (transactionList.contains(transaction)) {
//                    transactionList.remove(transaction);
//                }
//            }
//        }

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
            log.info("Starting process of attaching transaction with hash:{}: trustScore: {}", transaction.getHash(), transaction.getSenderTrustScore());
            // TODO: Get The transaction trust score from trust score node.

//            final ConcurrentHashMap<Integer, List<TransactionData>> localThreadTrustScoreToSourceListMapping =
//                    new ConcurrentHashMap<>(trustScoreToSourceListMapping);


//        for (final List<TransactionData> finalSelectedSourcesForAttachment = new Vector<TransactionData>(selectedSourcesForAttachment);
//             finalSelectedSourcesForAttachment.size() == 0 &&  localThreadTustScoreToSourceListMapping.size() > 0 ; ) {
            AtomicBoolean executed = new AtomicBoolean();

            ConcurrentHashMap<Integer, List<TransactionData>> localThreadTrustScoreToSourceListMapping = null;


            List<TransactionData> selectedSourcesForAttachment = null;
            executed.set(true);
            int localTrustScoreToSourceListMappingSum;
            synchronized (locker) {
                localThreadTrustScoreToSourceListMapping = new ConcurrentHashMap<>(trustScoreToSourceListMapping);
                localTrustScoreToSourceListMappingSum = getTotalNumberOfSources();
            }
            log.info("num of sources: {}", localTrustScoreToSourceListMappingSum);
            if (localTrustScoreToSourceListMappingSum > 0) {

                // Selection of sources
                selectedSourcesForAttachment = sourceSelector.selectSourcesForAttachment(localThreadTrustScoreToSourceListMapping,
                        transaction.getSenderTrustScore(),
                        transaction.getProcessStartTime(),
                        10, // TODO: get value from config file and/or dynamic
                        20); // TODO:  get value from config file and/or dynamic
                if (selectedSourcesForAttachment.size() == 0) {
                    executed.set(false);
                    log.info("in attachment process of transaction with hash:{}: whe have source: {}, waiting for more transactions", transaction.getHash());
                } else {
                    log.info("in attachment process of transaction with hash:{}: whe have source: {}", transaction.getHash());
                }
            } else {
                log.info("in attachment process of transaction with hash:{}: , there is no other transactions in cluster. No need for attachment", transaction.getHash());
            }

            if (executed.get()) {

                if (localTrustScoreToSourceListMappingSum > 0) {

                    // Attache sources
                    attachmentProcess(localThreadTrustScoreToSourceListMapping, selectedSourcesForAttachment, transaction);
                }
                // updating transaction collections with the new transaction
                addNewTransactionToAllCollections(transaction);

                transaction.setProcessEndTime(new Date());
                log.info("Ending process of attaching transaction with hash:{} trustScore: {}  LeftParentHash:{}  RightParentHash:{} !!!",
                        transaction.getHash(),
                        transaction.getSenderTrustScore(),
                        transaction.getLeftParent(),
                        transaction.getRightParent());

            }


        } catch (Exception e) {
            log.error(e.toString());
        }

        return true;
    }


    private void attachmentProcess(ConcurrentHashMap<Integer, List<TransactionData>> localThreadTrustScoreToSourceListMapping,
                                   List<TransactionData> selectedSourcesForAttachment,
                                   TransactionData transaction) {
        if (localThreadTrustScoreToSourceListMapping.size() > 1) {

            for (TransactionData sourceTransaction : selectedSourcesForAttachment) {
                log.info("in attachment process of transaction with hash:{}: whe have source: {}", transaction.getHash(), sourceTransaction.getHash());
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

                    for (Hash hash : transactionConsensusConfirmed) {
                        log.info("transaction with hash:{}: is confirmed with trustScore: {} ans totalTrustScore: {} !!!",
                                hashToUnTccConfirmationTransactionsMapping.get(hash).getHash(),
                                hashToUnTccConfirmationTransactionsMapping.get(hash).getSenderTrustScore(),
                                hashToUnTccConfirmationTransactionsMapping.get(hash).getTotalTrustScore());
                        deleteTransactionFromHashToUnTccConfirmedTransactionsMapping(hash);
                        //balanceService.
                    }
                    try {
                        TimeUnit.SECONDS.sleep(15);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


    }
}

