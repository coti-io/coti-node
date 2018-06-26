package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.model.Transactions;
import io.coti.cotinode.service.interfaces.IClusterService;
import io.coti.cotinode.service.interfaces.ISourceSelector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClusterService implements IClusterService {

    private static Object locker = new Object();

    @Value("${cluster.delay.after.tcc}")
    private int delayTimeAfterTccProcess;

    @Autowired
    private QueueService queueService;

    @Autowired
    private Transactions dbTransactions;

    @Autowired
    private ISourceSelector sourceSelector;

    @Autowired
    private TccConfirmationService tccConfirmationService;

    private ConcurrentHashMap<Hash, TransactionData> hashToUnTccConfirmationTransactionsMapping;
    private final List<Vector<TransactionData>> sourceListsByTrustScore = new ArrayList<Vector<TransactionData>>();
    private Executor executor;


    @PostConstruct
    public void initCluster() {
        initCluster(new ArrayList<>());
    }

    @Override
    public void initCluster(List<Hash> notConfirmTransactions) {
        try {
            executor = Executors.newSingleThreadScheduledExecutor();
            hashToUnTccConfirmationTransactionsMapping = new ConcurrentHashMap<>();
            dbTransactions.init();
            setUnTccConfirmedTransactions(getTransactionsByHashFromDb(notConfirmTransactions));
            initSources();
            setTrustScoreToSourceListMapping(hashToUnTccConfirmationTransactionsMapping);

            trustScoreConsensusProcess();
        } catch (Exception e) {
            log.error("Error in initCluster", e);
        }
    }

    private void initSources() {
        for(int i = 0; i <= 100; i++){
            sourceListsByTrustScore.add(new Vector<>());
        }
        for (TransactionData transaction : hashToUnTccConfirmationTransactionsMapping.values()) {
            addToTrustScoreToSourceListMap(transaction);
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

    private void addNewTransactionToMemoryStorage(TransactionData transaction) {
        synchronized (locker) {
            // add to unTccConfirmedTransaction map
            hashToUnTccConfirmationTransactionsMapping.put(transaction.getHash(), transaction);
            //  add to TrustScoreToSourceList map
            addToTrustScoreToSourceListMap(transaction);
        }
    }

    private void setUnTccConfirmedTransactions(List<TransactionData> notConfirmTransactions) {

        this.hashToUnTccConfirmationTransactionsMapping.
                putAll(notConfirmTransactions.stream().
                        //filter(x -> !x.isTransactionConsensus()).
                                collect(Collectors.
                                toMap(TransactionData::getHash, Function.identity())));
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
                List<TransactionData> transactionTrustScoreList = sourceListsByTrustScore.get(transaction.getRoundedSenderTrustScore());
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


    public TransactionData addTransactionDataToSources(TransactionData zeroSpendTransaction) {
        addNewTransactionToMemoryStorage(zeroSpendTransaction);
        return zeroSpendTransaction;
    }

    @Override
    public TransactionData selectSources(TransactionData transactionData) {
        ConcurrentHashMap<Integer, List<TransactionData>> trustScoreToTransactionMappingSnapshot =
                new ConcurrentHashMap<>(trustScoreToSourceListMapping);

        List<TransactionData> selectedSourcesForAttachment =
                sourceSelector.selectSourcesForAttachment(
                        trustScoreToTransactionMappingSnapshot,
                        transactionData.getSenderTrustScore());

        if (selectedSourcesForAttachment.size() > 0) {
            transactionData.setLeftParentHash(selectedSourcesForAttachment.get(0).getHash());
        }
        if (selectedSourcesForAttachment.size() > 1) {
            transactionData.setRightParentHash(selectedSourcesForAttachment.get(1).getHash());
        }

        return transactionData;
    }
}
