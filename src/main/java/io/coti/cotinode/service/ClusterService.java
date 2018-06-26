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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service

public class ClusterService implements IClusterService {

    private static Object locker = new Object();
    private final Vector<TransactionData>[] sourceListsByTrustScore = new Vector[101];

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
            trustScoreConsensusProcess();
        } catch (Exception e) {
            log.error("Error in initCluster", e);
        }
    }

    private void initSources() {
        for (int i = 0; i <= 100; i++) {
            sourceListsByTrustScore[i] = (new Vector<>());
        }
        for (TransactionData transaction : hashToUnTccConfirmationTransactionsMapping.values()) {
            List<TransactionData> sameTrustScoreTransactions =
                    sourceListsByTrustScore[transaction.getRoundedSenderTrustScore()];
            if (transaction.isSource() && !sameTrustScoreTransactions.contains(transaction)) {
                sameTrustScoreTransactions.add(transaction);
            }
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
            if (transaction.isSource()) {
                List<TransactionData> transactionTrustScoreList =
                        sourceListsByTrustScore[transaction.getRoundedSenderTrustScore()];
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

            removeTransactionFromSourcesList(transaction);
        }
    }

    private void removeTransactionFromSourcesList(TransactionData transaction) {
        sourceListsByTrustScore[transaction.getRoundedSenderTrustScore()].remove(transaction);
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
            removeTransactionFromSourcesList(dbTransactions.getByHash(leftParentHash));
        }

        if (rightParentHash != null && hashToUnTccConfirmationTransactionsMapping.get(rightParentHash) != null) {
            hashToUnTccConfirmationTransactionsMapping.get(rightParentHash).addToChildrenTransactions(childHash);
            removeTransactionFromSourcesList(dbTransactions.getByHash(rightParentHash));
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
        Vector<TransactionData>[] trustScoreToTransactionMappingSnapshot = new Vector[101];
        for(int i = 0; i <= 100; i++){
            trustScoreToTransactionMappingSnapshot[i] = (Vector<TransactionData>) sourceListsByTrustScore[i].clone();
        }

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
