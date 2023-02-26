package io.coti.basenode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.ChunkException;
import io.coti.basenode.exceptions.TransactionValidationException;
import io.coti.basenode.http.*;
import io.coti.basenode.http.data.ExtendedTransactionResponseData;
import io.coti.basenode.http.data.ReducedTransactionResponseData;
import io.coti.basenode.http.data.TransactionResponseData;
import io.coti.basenode.http.data.interfaces.ITransactionResponseData;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.interfaces.ITransactionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.FluxSink;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static io.coti.basenode.constants.BaseNodeMessages.*;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;
import static io.coti.basenode.services.BaseNodeServiceManager.*;

@Slf4j
@Service
public class BaseNodeTransactionService implements ITransactionService {

    protected Map<TransactionData, Boolean> postponedTransactionMap = new ConcurrentHashMap<>();  // true/false means new from full node or propagated transaction
    private final LockData transactionLockData = new LockData();
    protected static final long REJECTED_TRANSACTIONS_TTL = Duration.ofDays(30).getSeconds();

    @Override
    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public void getTransactionBatch(long startingIndex, long endingIndex, HttpServletResponse response,
                                    boolean isExtended, boolean isIncludeRuntimeTrustScore) {
        CustomHttpServletResponse customResponse = new CustomHttpServletResponse(response);
        PrintWriter output;
        try {
            if ((startingIndex < 0) || (startingIndex > endingIndex && endingIndex != -1) || (startingIndex > transactionIndexService.getLastTransactionIndexData().getIndex())
                    || (transactionIndexService.getLastTransactionIndexData().getIndex()) < endingIndex) {
                customResponse.printResponse(new Response(
                        TRANSACTION_INDEX_INVALID,
                        STATUS_ERROR), HttpStatus.BAD_REQUEST.value());
                return;
            }
            output = customResponse.getWriter();
        } catch (IOException ioException) {
            log.error(ioException.getMessage());
            return;
        }

        AtomicBoolean firstTransactionSent = new AtomicBoolean(false);
        boolean isChunkStarted = false;
        try {
            chunkService.startOfChunk(output);
            isChunkStarted = true;
            long limit = (endingIndex == -1) ? transactionIndexService.getLastTransactionIndexData().getIndex() : endingIndex;
            for (long i = startingIndex; i <= limit; i++) {
                sendTransactionResponse(transactionIndexes.getByHash(new Hash(i)).getTransactionHash(), firstTransactionSent, output, isExtended, isIncludeRuntimeTrustScore);
            }
            chunkService.endOfChunk(output);

        } catch (Exception e) {
            log.error(e.toString());
            try {
                if (firstTransactionSent.get()) {
                    chunkService.sendChunk(",", output);
                }
                customResponse.printResponse(new Response(
                        TRANSACTION_RESPONSE_ERROR,
                        STATUS_ERROR), HttpStatus.INTERNAL_SERVER_ERROR.value());
                if (isChunkStarted) {
                    chunkService.endOfChunk(output);
                }
            } catch (IOException ioException) {
                log.error(ioException.getMessage());
            }
        }
    }

    @Override
    public void getTransactionBatch(long startingIndex, HttpServletResponse response) {

        AtomicLong transactionNumber = new AtomicLong(0);
        Thread monitorTransactionBatch = monitorTransactionBatch(Thread.currentThread().getId(), transactionNumber);

        try {
            ServletOutputStream output = response.getOutputStream();

            monitorTransactionBatch.start();

            if (startingIndex <= transactionIndexService.getLastTransactionIndexData().getIndex()) {
                for (long i = startingIndex; i <= transactionIndexService.getLastTransactionIndexData().getIndex(); i++) {
                    output.write(jacksonSerializer.serialize(transactions.getByHash(transactionIndexes.getByHash(new Hash(i)).getTransactionHash())));
                    output.flush();
                    transactionNumber.incrementAndGet();
                }
            }
            for (Hash hash : nodeTransactionHelper.getNoneIndexedTransactionHashes()) {
                output.write(jacksonSerializer.serialize(transactions.getByHash(hash)));
                output.flush();
                transactionNumber.incrementAndGet();

            }

        } catch (Exception e) {
            log.error("Error sending transaction batch");
            log.error(e.getMessage());
        } finally {
            if (monitorTransactionBatch.isAlive()) {
                monitorTransactionBatch.interrupt();
            }
        }
    }

    @Override
    public void getTransactionBatch(long startingIndex, FluxSink<byte[]> sink) {
        AtomicLong transactionNumber = new AtomicLong(0);
        Thread monitorTransactionBatch = monitorTransactionBatch(Thread.currentThread().getId(), transactionNumber);

        try {
            monitorTransactionBatch.start();

            if (startingIndex <= transactionIndexService.getLastTransactionIndexData().getIndex()) {
                for (long i = startingIndex; i <= transactionIndexService.getLastTransactionIndexData().getIndex(); i++) {
                    sink.next((jacksonSerializer.serialize(transactions.getByHash(transactionIndexes.getByHash(new Hash(i)).getTransactionHash()))));
                    transactionNumber.incrementAndGet();
                }
            }

            for (Hash hash : nodeTransactionHelper.getNoneIndexedTransactionHashes()) {
                sink.next(jacksonSerializer.serialize((transactions.getByHash(hash))));
                transactionNumber.incrementAndGet();

            }
            sink.complete();
        } catch (Exception e) {
            log.error("Error sending reactive transaction batch");
            log.error(e.getMessage());
        } finally {
            if (monitorTransactionBatch.isAlive()) {
                monitorTransactionBatch.interrupt();
            }
        }
    }

    private Thread monitorTransactionBatch(long threadId, AtomicLong transactionNumber) {
        return new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                log.info("Transaction batch: thread id = {}, transactionNumber= {}", threadId, transactionNumber);
            }
        });
    }

    @Override
    public ResponseEntity<IResponse> getNoneIndexedTransactions() {
        try {
            Set<Hash> noneIndexedTransactionHashes = nodeTransactionHelper.getNoneIndexedTransactionHashes();
            List<TransactionData> noneIndexedTransactions = new ArrayList<>();
            noneIndexedTransactionHashes.forEach(transactionHash -> {
                        TransactionData transactionData = transactions.getByHash(transactionHash);
                        if (transactionData != null) {
                            noneIndexedTransactions.add(transactionData);
                        }
                    }

            );

            return ResponseEntity.ok(new GetTransactionsResponse(noneIndexedTransactions));
        } catch (Exception e) {
            log.info("Exception while getting none indexed transactions", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(
                            TRANSACTION_NONE_INDEXED_SERVER_ERROR,
                            STATUS_ERROR));
        }
    }

    @Override
    public void getNoneIndexedTransactionBatch(HttpServletResponse response, boolean isExtended) {
        try {
            Set<Hash> noneIndexedTransactionHashes = nodeTransactionHelper.getNoneIndexedTransactionHashes();
            PrintWriter output = response.getWriter();
            chunkService.startOfChunk(output);
            AtomicBoolean firstTransactionSent = new AtomicBoolean(false);

            noneIndexedTransactionHashes.forEach(transactionHash ->
                    sendTransactionResponse(transactionHash, firstTransactionSent, output, isExtended, true)
            );
            chunkService.endOfChunk(output);
        } catch (Exception e) {
            log.error("Error sending none-indexed transaction batch");
            log.error(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<IResponse> getPostponedTransactions() {
        try {
            ConcurrentHashMap<Hash, TransactionData> postponedTransactionCluster = new ConcurrentHashMap<>();
            postponedTransactionCluster.putAll(postponedTransactionMap.keySet().stream().collect(Collectors.toMap(TransactionData::getHash, transactionData -> transactionData)));
            LinkedList<TransactionData> topologicalOrderedPostponedTransactions = new LinkedList<>();
            clusterHelper.sortByTopologicalOrder(postponedTransactionCluster, topologicalOrderedPostponedTransactions);
            return ResponseEntity.ok(new GetExtendedTransactionsResponse(topologicalOrderedPostponedTransactions));
        } catch (Exception e) {
            log.info("Exception while getting postponed transactions", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(
                            TRANSACTION_POSTPONED_SERVER_ERROR,
                            STATUS_ERROR));
        }
    }

    @Override
    public void handlePropagatedTransaction(TransactionData transactionData) {
        AtomicBoolean isTransactionAlreadyPropagated = new AtomicBoolean(false);

        try {
            checkTransactionAlreadyPropagatedAndStartHandle(transactionData, isTransactionAlreadyPropagated);
            if (isTransactionAlreadyPropagated.get()) {
                removeTransactionHashFromUnconfirmed(transactionData);
                log.debug("Transaction already exists: {}", transactionData.getHash());
                return;
            }
            if (!validationService.validatePropagatedTransactionDataIntegrity(transactionData)) {
                log.error("Data Integrity validation failed: {}", transactionData.getHash());
                return;
            }
            if (hasOneOfParentsMissing(transactionData)) {
                postponedTransactionMap.putIfAbsent(transactionData, false);
                return;
            }

            validateAndAttachTransaction(transactionData);

            continueHandlePropagatedTransaction(transactionData);
            nodeTransactionHelper.setTransactionStateToFinished(transactionData);
        } catch (TransactionValidationException ex) {
            log.error("Transaction Validation Exception while handling transaction {} : {}", transactionData.getHash(), ex.getMessage());
        } catch (Exception e) {
            log.error("Exception while handling transaction {} : ", transactionData.getHash(), e);
        } finally {
            if (!isTransactionAlreadyPropagated.get()) {
                boolean isTransactionFinished = nodeTransactionHelper.isTransactionFinished(transactionData);
                nodeTransactionHelper.endHandleTransaction(transactionData);
                if (isTransactionFinished) {
                    processPostponedTransactions(transactionData);
                }
            }
        }
    }

    protected void checkTransactionAlreadyPropagatedAndStartHandle(TransactionData transactionData, AtomicBoolean
            isTransactionAlreadyPropagated) {
        try {
            synchronized (transactionLockData.addLockToLockMap(transactionData.getHash())) {
                isTransactionAlreadyPropagated.set(nodeTransactionHelper.isTransactionAlreadyPropagated(transactionData));
                if (!isTransactionAlreadyPropagated.get()) {
                    if (!TransactionType.ZeroSpend.equals(transactionData.getType())) {
                        log.info("Starting handling for new transaction: {}", transactionData.getHash());
                    }
                    nodeTransactionHelper.startHandleTransaction(transactionData);
                }
            }
        } finally {
            transactionLockData.removeLockFromLocksMap(transactionData.getHash());
        }
    }

    protected void validateAndAttachTransaction(TransactionData transactionData) {
        if (!validationService.validateBalancesAndAddToPreBalance(transactionData)) {
            throw new TransactionValidationException(INSUFFICIENT_FUNDS_MESSAGE);
        }
        if (transactionData.getType().equals(TransactionType.TokenGeneration) && !validationService.validateCurrencyUniquenessAndAddUnconfirmedRecord(transactionData)) {
            throw new TransactionValidationException(NOT_UNIQUE_TOKEN_GENERATION_TRANSACTION);
        }
        if (transactionData.getType().equals(TransactionType.TokenMinting) && !validationService.validateTokenMintingAndAddToAllocatedAmount(transactionData)) {
            throw new TransactionValidationException(INSUFFICIENT_MINTING_FUNDS_MESSAGE);
        }
        if (transactionData.getType().equals(TransactionType.EventHardFork) && !validationService.validateEventHardFork(transactionData)) {
            throw new TransactionValidationException(EVENT_HARD_FORK_ERROR);
        }
        nodeTransactionHelper.attachTransactionToCluster(transactionData);
        nodeTransactionHelper.setTransactionStateToSaved(transactionData);
    }

    public void removeTransactionHashFromUnconfirmed(TransactionData transactionData) {
        // Implemented by Full Node
    }

    @Override
    public void handlePropagatedRejectedTransaction(RejectedTransactionData rejectedTransactionData) {
        // Implemented by Full Node
    }

    protected void processPostponedTransactions(TransactionData transactionData) {
        DspConsensusResult postponedDspConsensusResult = dspVoteService.getPostponedDspConsensusResult(transactionData.getHash());
        if (postponedDspConsensusResult != null) {
            dspVoteService.handleVoteConclusion(postponedDspConsensusResult);
        }
        Map<TransactionData, Boolean> postponedParentTransactionMap = getPostponedChildrenTransactionMap(transactionData.getHash(), postponedTransactionMap);
        postponedParentTransactionMap.forEach((postponedTransaction, isTransactionFromFullNode) -> {
            log.debug("Handling postponed transaction : {}, parent of transaction: {}", postponedTransaction.getHash(), transactionData.getHash());
            postponedTransactionMap.remove(postponedTransaction);
            handlePostponedTransaction(postponedTransaction, isTransactionFromFullNode);
        });
    }

    protected Map<TransactionData, Boolean> getPostponedChildrenTransactionMap(Hash transactionDataHash, Map<TransactionData, Boolean> postponedTransactionMap) {
        return postponedTransactionMap.entrySet().stream().filter(
                        postponedTransactionMapEntry ->
                                (postponedTransactionMapEntry.getKey().getRightParentHash() != null
                                        && postponedTransactionMapEntry.getKey().getRightParentHash().equals(transactionDataHash))
                                        || (postponedTransactionMapEntry.getKey().getLeftParentHash() != null
                                        && postponedTransactionMapEntry.getKey().getLeftParentHash().equals(transactionDataHash)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    protected void handlePostponedTransaction(TransactionData postponedTransaction,
                                              boolean isTransactionFromFullNode) {
        if (!isTransactionFromFullNode) {
            handlePropagatedTransaction(postponedTransaction);
        }
    }

    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
        // implemented by subclasses
    }

    @Override
    public void handleMissingTransaction(TransactionData
                                                 transactionData, Set<Hash> trustChainUnconfirmedExistingTransactionHashes, EnumMap<InitializationTransactionHandlerType, ExecutorData> missingTransactionExecutorMap) {
        boolean transactionExists = nodeTransactionHelper.isTransactionExists(transactionData);
        transactions.put(transactionData);
        if (!transactionExists) {
            missingTransactionExecutorMap.get(InitializationTransactionHandlerType.TRANSACTION).submit(() -> {
                addDataToMemory(transactionData);
                continueHandleMissingTransaction(transactionData);
            });
            missingTransactionExecutorMap.get(InitializationTransactionHandlerType.CONFIRMATION).submit(() ->
            {
                confirmationService.insertMissingTransaction(transactionData);
                currencyService.handleMissingTransaction(transactionData);
                mintingService.handleMissingTransaction(transactionData);
            });
            nodeTransactionHelper.incrementTotalTransactions();
        } else {
            missingTransactionExecutorMap.get(InitializationTransactionHandlerType.CONFIRMATION).submit(() ->
            {
                confirmationService.insertMissingConfirmation(transactionData, trustChainUnconfirmedExistingTransactionHashes);
                currencyService.handleMissingTransaction(transactionData);
                mintingService.handleMissingTransaction(transactionData);
            });
        }
        nodeEventService.handleMissingTransaction(transactionData);
        missingTransactionExecutorMap.get(InitializationTransactionHandlerType.CLUSTER).submit(() -> clusterService.addMissingTransactionOnInit(transactionData, trustChainUnconfirmedExistingTransactionHashes));
    }

    protected void continueHandleMissingTransaction(TransactionData transactionData) {
        log.debug("Continue handle missing transaction {} by base node", transactionData.getHash());
    }

    @Override
    public Thread monitorTransactionThread(String type, AtomicLong transactionNumber, AtomicLong
            receivedTransactionNumber, String monitorThreadName) {
        return new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(5000);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                if (receivedTransactionNumber != null) {
                    log.info("Received {} transactions: {}, inserted transactions: {}", type, receivedTransactionNumber, transactionNumber);
                } else {
                    log.info("Inserted {} transactions: {}", type, transactionNumber);
                }
            }
        }, monitorThreadName);
    }

    public void addDataToMemory(TransactionData transactionData) {
        log.debug("Adding the transaction {} to explorer indexes by base node", transactionData.getHash());
    }

    @Override
    public ResponseEntity<IResponse> setIndexToTransactions(SetIndexesRequest setIndexesRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void shutdown() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseEntity<IResponse> getRejectedTransactions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handleNewTransactionFromFullNode(TransactionData data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseEntity<Response> addNewTransaction(AddTransactionRequest addTransactionRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseEntity<IResponse> repropagateTransactionByWallet(RepropagateTransactionRequest repropagateTransactionRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseEntity<IResponse> getTransactionDetails(Hash transactionHash, boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void getTransactions(GetTransactionsRequest getTransactionsRequest, HttpServletResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseEntity<IResponse> getAddressTransactions(Hash address) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void getAddressTransactionBatch(GetAddressTransactionBatchRequest getAddressTransactionBatchRequest, HttpServletResponse response, boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void getAddressTransactionBatchByTimestamp(GetAddressTransactionBatchByTimestampRequest getAddressTransactionBatchByTimestampRequest, HttpServletResponse response, boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void getAddressTransactionBatchByDate(GetAddressTransactionBatchByDateRequest getAddressTransactionBatchByDateRequest, HttpServletResponse response, boolean b) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void getAddressRejectedTransactionBatch(GetAddressTransactionBatchRequest getAddressTransactionBatchRequest, HttpServletResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseEntity<IResponse> getLastTransactions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseEntity<IResponse> getTotalTransactions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseEntity<IResponse> getTransactionsByPage(int page) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseEntity<IResponse> repropagateTransactionByAdmin(RepropagateTransactionByAdminRequest repropagateTransactionByAdminRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseEntity<IResponse> setReceiverBaseTransactionOwner(TransactionRequest transactionRequest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void getTransactionsByAddress(GetTransactionsByAddressRequest getTransactionsByAddressRequest, HttpServletResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void getTransactionsByDate(GetTransactionsByDateRequest getTransactionsByDateRequest, HttpServletResponse response) {
        throw new UnsupportedOperationException();
    }

    public void removeDataFromMemory(TransactionData transactionData) {
        log.debug("Removing the transaction {} from explorer indexes by base node", transactionData.getHash());
    }

    protected boolean hasOneOfParentsMissing(TransactionData transactionData) {
        return (transactionData.getLeftParentHash() != null && transactions.getByHash(transactionData.getLeftParentHash()) == null) ||
                (transactionData.getRightParentHash() != null && transactions.getByHash(transactionData.getRightParentHash()) == null);
    }

    public int totalPostponedTransactions() {
        return postponedTransactionMap.size();
    }

    protected void sendTransactionResponse(Hash transactionHash, AtomicBoolean firstTransactionSent, PrintWriter
            output, boolean isExtended, boolean isIncludeRuntimeTrustScore) {
        sendTransactionResponse(transactionHash, firstTransactionSent, output, null, false, isExtended, isIncludeRuntimeTrustScore);
    }

    protected void sendTransactionResponse(Hash transactionHash, AtomicBoolean firstTransactionSent, PrintWriter
            output, Hash addressHash, boolean reduced, boolean extended, boolean includeRuntimeTrustScore) {
        try {
            TransactionData transactionData = transactions.getByHash(transactionHash);
            if (transactionData != null) {
                setRunTimeTrustChainTrustScore(transactionData, includeRuntimeTrustScore);
                ITransactionResponseData transactionResponseData;
                if (reduced) {
                    transactionResponseData = new ReducedTransactionResponseData(transactionData, addressHash);
                } else {
                    transactionResponseData = extended ? new ExtendedTransactionResponseData(transactionData) : new TransactionResponseData(transactionData);
                }
                if (firstTransactionSent.get()) {
                    chunkService.sendChunk(",", output);
                } else {
                    firstTransactionSent.set(true);
                }
                chunkService.sendChunk(new CustomGson().getInstance().toJson(transactionResponseData), output);
            }
        } catch (ChunkException e) {
            log.error("Error at transaction response data for {}", transactionHash);
            throw e;
        } catch (Exception e) {
            log.error("Error at transaction response data for {}", transactionHash);
            log.error(e.getMessage());
        }
    }

    private void setRunTimeTrustChainTrustScore(TransactionData transactionData, boolean includeRuntimeTrustScore) {
        if (!transactionData.isTrustChainConsensus() && includeRuntimeTrustScore) {
            double runtimeTrustChainTrustScore = clusterService.getRuntimeTrustChainTrustScore(transactionData.getHash());
            if (runtimeTrustChainTrustScore > transactionData.getTrustChainTrustScore()) {
                transactionData.setTrustChainTrustScore(runtimeTrustChainTrustScore);
            }
        }
    }
}
