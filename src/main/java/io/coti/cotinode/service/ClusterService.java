package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.model.Transactions;
import io.coti.cotinode.service.interfaces.IBalanceService;
import io.coti.cotinode.service.interfaces.IClusterService;
import io.coti.cotinode.service.interfaces.ISourceSelector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClusterService implements IClusterService {

    private static Object locker = new Object();

    @Value("${cluster.delay.after.tcc}")
    private int delayTimeAfterTccProcess;

    @Autowired
    QueueService queueService;

    @Autowired
    private Transactions dbTransactions;

    @Autowired
    private ISourceSelector sourceSelector;

    @Autowired
    private TccConfirmationService tccConfirmationService;

    private ConcurrentHashMap<Hash, TransactionData> hashToUnTccConfirmationTransactionsMapping;
    private ConcurrentHashMap<Integer, List<TransactionData>> trustScoreToSourceListMapping;
    private Executor executor;

    private void handleUnconfirmedFromQueueTransactions() {
        ConcurrentLinkedQueue<Hash> UnconfirmedTransactionsHashFromQueue = queueService.getTccQueue();

        for (Hash hash : UnconfirmedTransactionsHashFromQueue) {
            TransactionData transaction = dbTransactions.getByHash(hash);
            attachmentProcess(transaction);
        }
        queueService.removeTccQueue();
    }

    private void addNewTransactionToMemoryStorage(TransactionData transaction) {
        synchronized (locker) {
            // add to unTccConfirmedTransaction map
            hashToUnTccConfirmationTransactionsMapping.put(transaction.getHash(), transaction);
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

    private void setUnTccConfirmedTransactions(List<TransactionData> notConfirmTransactions) {

        this.hashToUnTccConfirmationTransactionsMapping.
                putAll(notConfirmTransactions.stream().
                        //filter(x -> !x.isTransactionConsensus()).
                                collect(Collectors.
                                toMap(TransactionData::getHash, Function.identity())));
    }

    private void setTrustScoreToSourceListMapping(ConcurrentHashMap<Hash, TransactionData> hashToUnconfirmedTransactionsMapping) {
        this.trustScoreToSourceListMapping = new ConcurrentHashMap<>();
        for (int i = 1; i <= 100; i++) {
            trustScoreToSourceListMapping.put(i, new Vector<TransactionData>());
        }

        for (TransactionData transaction : hashToUnconfirmedTransactionsMapping.values()) {
            addToTrustScoreToSourceListMap(transaction);
        }
    }

    private List<TransactionData> getTransactionsByHashFromDb(List<Hash> notConfirmTransactions) {
        List<TransactionData> transactions = new Vector<>();
        notConfirmTransactions.forEach(
                hash -> transactions.add(dbTransactions.getByHash(hash))
        );
        return transactions;
    }

    private void addToTrustScoreToSourceListMap(TransactionData transaction) {

        synchronized (locker) {
            if (transaction.isSource() && transaction.getSenderTrustScore() >= 1 && transaction.getSenderTrustScore() <= 100) {
                List<TransactionData> transactionTrustScoreList = trustScoreToSourceListMapping.get(transaction.GetRoundedSenderTrustScore());
                if (!transactionTrustScoreList.contains(transaction)) {
                    transactionTrustScoreList.add(transaction);
                }
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
    }

    private List<TransactionData> getAllSourceTransactions() {
        return hashToUnTccConfirmationTransactionsMapping.values().stream().
                filter(TransactionData::isSource).collect(Collectors.toList());
    }


    private void attachmentProcess(TransactionData attachedTransactionFromDb) {
        attachedTransactionFromDb.setAttachmentTime(new Date());

        Hash childHash = attachedTransactionFromDb.getHash();
        Hash leftParentHash = attachedTransactionFromDb.getLeftParentHash();
        Hash rightParentHash = attachedTransactionFromDb.getRightParentHash();

        synchronized (locker) {
            attachedTransactionFromDb.setLeftParentHash(leftParentHash);
            attachedTransactionFromDb.setRightParentHash(rightParentHash);
        }

        if (leftParentHash != null && hashToUnTccConfirmationTransactionsMapping.get(leftParentHash) != null) {
            hashToUnTccConfirmationTransactionsMapping.get(leftParentHash).addToChildrenTransactions(childHash);
            deleteTrustScoreToSourceListMapping(dbTransactions.getByHash(leftParentHash));
        }

        if (rightParentHash != null && hashToUnTccConfirmationTransactionsMapping.get(rightParentHash) != null) {
            hashToUnTccConfirmationTransactionsMapping.get(rightParentHash).addToChildrenTransactions(childHash);
            deleteTrustScoreToSourceListMapping(dbTransactions.getByHash(rightParentHash));
        }

        addNewTransactionToMemoryStorage(attachedTransactionFromDb);
    }

    private void trustScoreConsensusProcess() {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    handleUnconfirmedFromQueueTransactions();
                    tccConfirmationService.init(hashToUnTccConfirmationTransactionsMapping);
                    tccConfirmationService.topologicalSorting();
                    List<Hash> transactionConsensusConfirmed = tccConfirmationService.setTransactionConsensus();

                    for (Hash hash : transactionConsensusConfirmed) {
                        deleteTransactionFromHashToUnTccConfirmedTransactionsMapping(hash);
                        queueService.addToUpdateBalanceQueue(hash);

                    }
                    try {
                        TimeUnit.SECONDS.sleep(delayTimeAfterTccProcess);
                    } catch (InterruptedException e) {
                        log.error(e.toString());
                    }
                }
            }
        });
    }

    @PostConstruct
    void initCluster(){
        initCluster(new ArrayList<>());
    }

    @Override
    public void initCluster(List<Hash> notConfirmTransactions) {

        try {
            executor = Executors.newSingleThreadScheduledExecutor();
            hashToUnTccConfirmationTransactionsMapping = new ConcurrentHashMap<>();
            trustScoreToSourceListMapping = new ConcurrentHashMap<>();

            dbTransactions.init();
            setUnTccConfirmedTransactions(getTransactionsByHashFromDb(notConfirmTransactions));
            setTrustScoreToSourceListMapping(hashToUnTccConfirmationTransactionsMapping);

            trustScoreConsensusProcess();
        } catch (Exception e) {
            log.error("Error in initCluster", e);
        }
    }


    // For Test Only !!!
    public boolean addNewTransaction(TransactionData transactionFromDb) {
        boolean thereAreSources = true;
        try {
            transactionFromDb.setProcessStartTime(new Date());

            // TODO: Get The transactionFromDb trust score from trust score node.

            ConcurrentHashMap<Integer, List<TransactionData>> localThreadTrustScoreToSourceListMapping = null;


            List<TransactionData> selectedSourcesForAttachment = null;
            int localTrustScoreToSourceListMappingSum;
            synchronized (locker) {
                localThreadTrustScoreToSourceListMapping = new ConcurrentHashMap<>(trustScoreToSourceListMapping);
                localTrustScoreToSourceListMappingSum = getTotalNumberOfSources();
            }
            if (localTrustScoreToSourceListMappingSum > 0) {

                // Selection of sources
                selectedSourcesForAttachment = sourceSelector.selectSourcesForAttachment(localThreadTrustScoreToSourceListMapping,
                        transactionFromDb.getSenderTrustScore(),
                        new Date(),
                        10, // TODO: get value from config file and/or dynamic
                        20); // TODO:  get value from config file and/or dynamic
                if (selectedSourcesForAttachment.size() == 0) {
                    log.info("in attachment process of transactionFromDb with hash:{}: waiting for more transactions ....", transactionFromDb.getHash());
                } else if (selectedSourcesForAttachment.size() == 1) {
                    log.info("in attachment process of transactionFromDb with hash:{}: whe have source {} !!! ",
                            transactionFromDb.getHash(), selectedSourcesForAttachment.get(0).getHash());
                    transactionFromDb.setLeftParentHash(selectedSourcesForAttachment.get(0).getHash());
                } else if (selectedSourcesForAttachment.size() == 2) {
                    log.info("in attachment process of transactionFromDb with hash:{}: whe have source: {}, {} !!!",
                            transactionFromDb.getHash(), selectedSourcesForAttachment.get(0).getHash(), selectedSourcesForAttachment.get(1).getHash());
                    transactionFromDb.setLeftParentHash(selectedSourcesForAttachment.get(0).getHash());
                    transactionFromDb.setLeftParentHash(selectedSourcesForAttachment.get(1).getHash());
                }
            } else {
                thereAreSources = false;
                log.info("in attachment process of transactionFromDb with hash:{}: , there is no other transactions in cluster. No need for attachment!!!", transactionFromDb.getHash());
            }

            /* Test only!!   */
            if (!thereAreSources || selectedSourcesForAttachment.size() > 0) {
                synchronized (locker) {
                    if (selectedSourcesForAttachment.size() > 0) {

                        transactionFromDb.setLeftParentHash(selectedSourcesForAttachment.get(0).getHash());
                        dbTransactions.getByHash(selectedSourcesForAttachment.get(0).getHash()).addToChildrenTransactions(transactionFromDb.getHash());


                        if (selectedSourcesForAttachment.size() > 1) {
                            transactionFromDb.setRightParentHash(selectedSourcesForAttachment.get(1).getHash());
                            dbTransactions.getByHash(selectedSourcesForAttachment.get(1).getHash()).addToChildrenTransactions(transactionFromDb.getHash());
                        }
                    }
                }

                dbTransactions.put(transactionFromDb);
                queueService.addToTccQueue(transactionFromDb.getHash());
            }
            /* end of Test section!!  */

        } catch (Exception e) {
            log.error("error in addNewTransaction", e);
        }


        return thereAreSources;
    }

    public TransactionData addTransactionDataToSources(TransactionData zeroSpendTransaction) {
        addNewTransactionToMemoryStorage(zeroSpendTransaction);
        return zeroSpendTransaction;
    }


    @Override
    public TransactionData selectSources(TransactionData transactionFromDb) {
        try {
            transactionFromDb.setProcessStartTime(new Date());

            // TODO: Get The transactionFromDb trust score from trust score node.

            ConcurrentHashMap<Integer, List<TransactionData>> localThreadTrustScoreToSourceListMapping = null;


            List<TransactionData> selectedSourcesForAttachment = null;
            int localTrustScoreToSourceListMappingSum;
            synchronized (locker) {
                localThreadTrustScoreToSourceListMapping = new ConcurrentHashMap<>(trustScoreToSourceListMapping);
                localTrustScoreToSourceListMappingSum = getTotalNumberOfSources();
            }
            if (localTrustScoreToSourceListMappingSum > 0) {

                // Selection of sources
                selectedSourcesForAttachment = sourceSelector.selectSourcesForAttachment(localThreadTrustScoreToSourceListMapping,
                        transactionFromDb.getSenderTrustScore(),
                        new Date(),
                        10, // TODO: get value from config file and/or dynamic
                        20); // TODO:  get value from config file and/or dynamic
                if (selectedSourcesForAttachment.size() == 0) {
                    log.info("in attachment process of transactionFromDb with hash:{}: waiting for more transactions ....", transactionFromDb.getHash());
                } else if (selectedSourcesForAttachment.size() == 1) {
                    log.info("in attachment process of transactionFromDb with hash:{}: whe have source {} !!! ",
                            transactionFromDb.getHash(), selectedSourcesForAttachment.get(0).getHash());
                    transactionFromDb.setLeftParentHash(selectedSourcesForAttachment.get(0).getHash());
                } else if (selectedSourcesForAttachment.size() == 2) {
                    log.info("in attachment process of transactionFromDb with hash:{}: whe have source: {}, {} !!!",
                            transactionFromDb.getHash(), selectedSourcesForAttachment.get(0).getHash(), selectedSourcesForAttachment.get(1).getHash());
                    transactionFromDb.setLeftParentHash(selectedSourcesForAttachment.get(0).getHash());
                    transactionFromDb.setLeftParentHash(selectedSourcesForAttachment.get(1).getHash());
                }
            }
        } catch (Exception e) {
            log.error("error in addNewTransaction", e);
        }


        return transactionFromDb;
    }

    @Override
    public boolean hasGenesisTransaction() {
        return false;
    }

}

