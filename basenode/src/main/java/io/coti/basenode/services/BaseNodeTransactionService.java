package io.coti.basenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.GetTransactionBatchResponse;
import io.coti.basenode.http.HttpJacksonSerializer;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.basenode.services.interfaces.ITransactionService;
import io.coti.basenode.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BaseNodeTransactionService implements ITransactionService {

    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private Transactions transactions;
    @Autowired
    private TransactionIndexService transactionIndexService;
    @Autowired
    private HttpJacksonSerializer jacksonSerializer;
    @Autowired
    private TransactionIndexes transactionIndexes;
    private Map<Hash, TransactionData> parentProcessingTransactions = new ConcurrentHashMap<>();
    private List<TransactionData> postponedTransactions = new LinkedList<>();
    private static final long MAXIMUM_CHUNK_SIZE = 10000;

    @Override
    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public void getTransactionBatch(long startingIndex, HttpServletResponse response) {
        try {
            PrintWriter output = response.getWriter();

            List<TransactionData> transactionsToSend = new ArrayList<>();
            AtomicLong transactionNumber = new AtomicLong(0);
            if (startingIndex > transactionIndexService.getLastTransactionIndexData().getIndex()) {
                output.print(new String(jacksonSerializer.serialize(new GetTransactionBatchResponse(transactionsToSend))));
                output.flush();
                return;
            }
            Thread monitorTransactionBatch = monitorTransactionBatch(Thread.currentThread().getId(), transactionNumber);
            monitorTransactionBatch.start();

            for (long i = startingIndex; i <= transactionIndexService.getLastTransactionIndexData().getIndex(); i++) {
                transactionsToSend.add(transactions.getByHash(transactionIndexes.getByHash(new Hash(i)).getTransactionHash()));
                transactionNumber.incrementAndGet();
                if (transactionNumber.get() % MAXIMUM_CHUNK_SIZE == 0) {
                    output.print(new String(jacksonSerializer.serialize(new GetTransactionBatchResponse(transactionsToSend))));
                    output.flush();
                    transactionsToSend.clear();
                }
            }
            transactionHelper.getNoneIndexedTransactionHashes().forEach(hash -> {
                transactionsToSend.add(transactions.getByHash(hash));
                transactionNumber.incrementAndGet();
                if (transactionNumber.get() % MAXIMUM_CHUNK_SIZE == 0) {
                    output.print(new String(jacksonSerializer.serialize(new GetTransactionBatchResponse(transactionsToSend))));
                    output.flush();
                    transactionsToSend.clear();
                }
            });

            if(!transactionsToSend.isEmpty()) {
                output.print(new String(jacksonSerializer.serialize(new GetTransactionBatchResponse(transactionsToSend))));
                output.flush();
            }
            monitorTransactionBatch.interrupt();
        //    transactionsToSend.sort(Comparator.comparing(transactionData -> transactionData.getAttachmentTime()));
        } catch (Exception e) {
            log.info("Error sending transaction batch");
            e.printStackTrace();
        }
    }

    private Thread monitorTransactionBatch(long threadId, AtomicLong transactionNumber) {
        return new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000);
                    log.info("Transaction batch: thread id = {}, transactionNumber= {}", threadId, transactionNumber);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.info("Transaction batch: thread id = {}, transactionNumber= {}", threadId, transactionNumber);
                }
            }
        });
    }

    @Override
    public void handlePropagatedTransaction(TransactionData transactionData) {
        if (transactionHelper.isTransactionAlreadyPropagated(transactionData)) {
            log.debug("Transaction already exists: {}", transactionData.getHash());
            return;
        }
        List<Hash> childrenTransactions = transactionData.getChildrenTransactionHashes();
        try {
            transactionHelper.startHandleTransaction(transactionData);
            while (hasOneOfParentsProcessing(transactionData)) {
                parentProcessingTransactions.put(transactionData.getHash(), transactionData);
                synchronized (transactionData) {
                    transactionData.wait();
                }
            }
            if (!validationService.validatePropagatedTransactionDataIntegrity(transactionData)) {
                log.error("Data Integrity validation failed: {}", transactionData.getHash());
                return;
            }
            if (hasOneOfParentsMissing(transactionData)) {
                postponedTransactions.add(transactionData);
                return;
            }
            if (!validationService.validateBalancesAndAddToPreBalance(transactionData)) {
                log.error("Balance check failed: {}", transactionData.getHash());
                return;
            }
            transactionHelper.attachTransactionToCluster(transactionData);
            transactionHelper.setTransactionStateToSaved(transactionData);

            continueHandlePropagatedTransaction(transactionData);
            transactionHelper.setTransactionStateToFinished(transactionData);
        } catch (InterruptedException e) {
            log.info("Transaction thread wait interrupted");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("Transaction propagation handler error:");
            e.printStackTrace();
        } finally {
            boolean isTransactionFinished = transactionHelper.isTransactionFinished(transactionData);
            transactionHelper.endHandleTransaction(transactionData);
            for (Hash childrenTransactionHash : childrenTransactions) {
                TransactionData childrenTransaction = parentProcessingTransactions.get(childrenTransactionHash);
                if (childrenTransaction != null)
                    synchronized (childrenTransaction) {
                        childrenTransaction.notify();
                        parentProcessingTransactions.remove(childrenTransactionHash);
                    }
            }
            if (isTransactionFinished) {
                List<TransactionData> postponedParentTransactions = postponedTransactions.stream().filter(
                        postponedTransactionData ->
                                (postponedTransactionData.getRightParentHash() != null && postponedTransactionData.getRightParentHash().equals(transactionData.getHash())) ||
                                        (postponedTransactionData.getLeftParentHash() != null && postponedTransactionData.getLeftParentHash().equals(transactionData.getHash())))
                        .collect(Collectors.toList());
                postponedParentTransactions.forEach(postponedTransaction -> {
                    log.debug("Handling postponed transaction : {}, parent of transaction: {}", postponedTransaction.getHash(), transactionData.getHash());
                    postponedTransactions.remove(postponedTransaction);
                    handlePropagatedTransaction(postponedTransaction);
                });
            }

        }

    }

    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
    }

    public void addToExplorerIndexes(TransactionData transactionData) {

    }

    private boolean hasOneOfParentsProcessing(TransactionData transactionData) {
        return (transactionData.getLeftParentHash() != null && transactionHelper.isTransactionHashProcessing(transactionData.getLeftParentHash())) ||
                (transactionData.getRightParentHash() != null && transactionHelper.isTransactionHashProcessing(transactionData.getRightParentHash()));
    }

    private boolean hasOneOfParentsMissing(TransactionData transactionData) {
        return (transactionData.getLeftParentHash() != null && transactions.getByHash(transactionData.getLeftParentHash()) == null) ||
                (transactionData.getRightParentHash() != null && transactions.getByHash(transactionData.getRightParentHash()) == null);
    }

    public int totalPostponedTransactions() {
        return postponedTransactions.size();
    }
}
