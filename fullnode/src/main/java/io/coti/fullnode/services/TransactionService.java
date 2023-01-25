package io.coti.fullnode.services;

import com.dictiography.collections.IndexedNavigableSet;
import com.dictiography.collections.IndexedTreeSet;
import com.google.common.collect.Sets;
import io.coti.basenode.data.*;
import io.coti.basenode.exceptions.PotException;
import io.coti.basenode.exceptions.TransactionException;
import io.coti.basenode.exceptions.TransactionValidationException;
import io.coti.basenode.http.*;
import io.coti.basenode.http.data.*;
import io.coti.basenode.http.data.interfaces.ITransactionResponseData;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.basenode.utilities.MemoryUtils;
import io.coti.fullnode.http.AddTransactionResponse;
import io.coti.fullnode.http.GetAddressTransactionHistoryResponse;
import io.coti.fullnode.http.GetExtendedTransactionResponse;
import io.coti.fullnode.http.GetTotalTransactionsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static io.coti.basenode.constants.BaseNodeMessages.*;
import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;
import static io.coti.fullnode.http.HttpStringConstants.EXPLORER_TRANSACTION_PAGE_ERROR;
import static io.coti.fullnode.http.HttpStringConstants.TRANSACTION_NO_DSP_IN_THE_NETWORK;
import static io.coti.fullnode.services.NodeServiceManager.*;

@Slf4j
@Service
@Primary
public class TransactionService extends BaseNodeTransactionService {

    private static final int EXPLORER_LAST_TRANSACTIONS_NUMBER = 20;
    private static final int EXPLORER_TRANSACTION_NUMBER_BY_PAGE = 10;

    private BlockingQueue<ExplorerTransactionData> explorerIndexQueue;
    private IndexedNavigableSet<ExplorerTransactionData> explorerIndexedTransactionSet;
    private BlockingQueue<TransactionData> addressTransactionsByAttachmentQueue;
    private Map<Hash, NavigableMap<Instant, Set<Hash>>> addressToTransactionsByAttachmentMap;
    private Map<Hash, Set<Hash>> addressToRejectedTransactionsMap;

    private static final AtomicInteger currentlyAddTransaction = new AtomicInteger(0);
    private final LockData transactionLockData = new LockData();
    @Value("${java.process.memory.limit:95}")
    private int javaProcessMemoryLimit;
    private final ReentrantReadWriteLock transactionReadWriteLock = new ReentrantReadWriteLock(true);

    @Override
    public void init() {
        startExplorerIndexThread();
        startAddressTransactionsByAttachmentThread();
        initAddressToRejectedTransactionsMap();
        super.init();
    }

    private void startExplorerIndexThread() {
        explorerIndexedTransactionSet = new IndexedTreeSet<>();
        explorerIndexQueue = new LinkedBlockingQueue<>();
        Thread explorerIndexThread = new Thread(this::updateExplorerIndex);
        explorerIndexThread.start();
    }

    private void startAddressTransactionsByAttachmentThread() {
        addressToTransactionsByAttachmentMap = new ConcurrentHashMap<>();
        addressTransactionsByAttachmentQueue = new LinkedBlockingQueue<>();
        Thread addressTransactionsByAttachmentThread = new Thread(this::updateAddressTransactionsByAttachment);
        addressTransactionsByAttachmentThread.start();
    }

    @Override
    public ResponseEntity<Response> addNewTransaction(AddTransactionRequest request) {
        TransactionData transactionData = nodeTransactionHelper.createNewTransaction(
                request.getBaseTransactions(),
                request.getHash(),
                request.getTransactionDescription(),
                request.getTrustScoreResults(),
                request.getCreateTime(),
                request.getSenderHash(),
                request.getSenderSignature(),
                request.getType());
        try {
            log.debug("New transaction request is being processed. Transaction Hash = {}", request.getHash());
            transactionReadWriteLock.readLock().lock();
            currentlyAddTransaction.incrementAndGet();
            synchronized (transactionLockData.addLockToLockMap(transactionData.getHash())) {
                if (((NetworkService) networkService).isNotConnectedToDspNodes()) {
                    log.error("FullNode is not connected to any DspNode. Rejecting transaction {}", transactionData.getHash());
                    return ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new Response(
                                    TRANSACTION_NO_DSP_IN_THE_NETWORK, STATUS_ERROR));
                }
                if (nodeTransactionHelper.isTransactionExists(transactionData)) {
                    log.debug("Received existing transaction: {}", transactionData.getHash());
                    return ResponseEntity
                            .status(HttpStatus.BAD_REQUEST)
                            .body(new Response(
                                    TRANSACTION_ALREADY_EXIST_MESSAGE, STATUS_ERROR));
                }
                nodeTransactionHelper.startHandleTransaction(transactionData);

                validateTransaction(transactionData);

                selectSources(transactionData);
                if (!transactionData.hasSources()) {
                    int selectSourceRetryCount = 0;
                    while (!transactionData.hasSources() && selectSourceRetryCount <= 20) {
                        log.debug("Could not find sources for transaction: {}. Retrying in 5 seconds.", transactionData.getHash());
                        TimeUnit.SECONDS.sleep(5);
                        selectSources(transactionData);
                        selectSourceRetryCount++;
                    }
                    if (!transactionData.hasSources()) {
                        log.info("No source found for transaction {} with trust score {}", transactionData.getHash(), transactionData.getSenderTrustScore());
                        return ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new Response(
                                        TRANSACTION_SOURCE_NOT_FOUND, STATUS_ERROR));
                    }
                }
                if (!validationService.validateSource(transactionData.getLeftParentHash()) ||
                        !validationService.validateSource(transactionData.getRightParentHash())) {
                    log.debug("Could not validate transaction source");
                }

                // ############   POT   ###########
                potAction(transactionData);
                // ################################

                transactionData.setAttachmentTime(Instant.now());
                transactionCrypto.signMessage(transactionData);
                nodeTransactionHelper.attachTransactionToCluster(transactionData);
                nodeTransactionHelper.setTransactionStateToSaved(transactionData);
                webSocketSender.notifyTransactionHistoryChange(transactionData, TransactionStatus.ATTACHED_TO_DAG);
                addDataToMemory(transactionData);
                ((NetworkService) networkService).sendDataToConnectedDspNodes(transactionData);
                transactionPropagationCheckService.addNewUnconfirmedTransaction(transactionData.getHash());
                nodeTransactionHelper.setTransactionStateToFinished(transactionData);
                return ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(new AddTransactionResponse(
                                TRANSACTION_CREATED_MESSAGE, transactionData.getAttachmentTime()));
            }
        } catch (TransactionValidationException e) {
            log.error("Transaction validation failed: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new Response(e.getMessage(), STATUS_ERROR));
        } catch (PotException e) {
            e.logMessage();
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new Response(e.getMessage() + " Cause: " + e.getCause().getMessage(), STATUS_ERROR));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Interrupted exception while adding transaction: {}", transactionData.getHash());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(TRANSACTION_INTERNAL_ERROR_MESSAGE, STATUS_ERROR));
        } catch (Exception e) {
            log.error("Exception while adding transaction: {}", transactionData.getHash());
            throw new TransactionException(e);
        } finally {
            nodeTransactionHelper.endHandleTransaction(transactionData);
            transactionLockData.removeLockFromLocksMap(transactionData.getHash());
            currentlyAddTransaction.decrementAndGet();
            transactionReadWriteLock.readLock().unlock();
        }
    }

    private boolean validateMultiDag(TransactionData transactionData) {
        for (BaseTransactionData transaction : transactionData.getBaseTransactions()) {
            if (!currencyService.isCurrencyHashAllowed(transaction.getCurrencyHash())) {
                return false;
            }
        }
        return true;
    }

    private void potAction(TransactionData transactionData) {
        try {
            potService.potAction(transactionData);
        } catch (Exception e) {
            throw new PotException("Error at POT for transaction: " + transactionData.getHash(), e);
        }
    }

    @Override
    public ResponseEntity<IResponse> repropagateTransactionByWallet(RepropagateTransactionRequest request) {

        if (!resendTransactionRequestCrypto.verifySignature(request)) {
            log.error("Signature validation failed for the request to resend transaction {}", request.getTransactionHash());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new Response(TRANSACTION_RESENT_INVALID_SIGNATURE_MESSAGE, STATUS_ERROR));
        }
        TransactionData transactionData = transactions.getByHash(request.getTransactionHash());
        if (transactionData == null) {
            log.error("Transaction {} requested to resend is not available in the database", request.getTransactionHash());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response(TRANSACTION_RESENT_NOT_AVAILABLE_MESSAGE, STATUS_ERROR));
        }
        if (!request.getSignerHash().equals(transactionData.getSenderHash())) {
            log.error("Transaction {} is requested to resend not by the transaction sender", request.getTransactionHash());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Response(TRANSACTION_RESENT_NOT_ALLOWED_MESSAGE, STATUS_ERROR));
        }
        return repropagateTransaction(transactionData);
    }

    @Override
    public ResponseEntity<IResponse> repropagateTransactionByAdmin(RepropagateTransactionByAdminRequest request) {
        TransactionData transactionData = transactions.getByHash(request.getTransactionHash());
        if (transactionData == null) {
            log.error("Transaction {} requested to resend is not available in the database", request.getTransactionHash());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response(TRANSACTION_RESENT_NOT_AVAILABLE_MESSAGE, STATUS_ERROR));
        }

        return repropagateTransaction(transactionData);

    }

    private ResponseEntity<IResponse> repropagateTransaction(TransactionData transactionData) {
        if (nodeTransactionHelper.isTransactionHashProcessing(transactionData.getHash())) {
            log.error("Transaction {} requested to resend is still being processed", transactionData.getHash());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new Response(TRANSACTION_RESENT_PROCESSING_MESSAGE, STATUS_ERROR));
        }

        ((NetworkService) networkService).sendDataToConnectedDspNodes(transactionData);
        log.info("Transaction {} is repropagated", transactionData.getHash());

        return ResponseEntity.status(HttpStatus.OK)
                .body(new Response(TRANSACTION_RESENT_MESSAGE));
    }

    public void selectSources(TransactionData transactionData) {
        clusterService.selectSources(transactionData);
        if (transactionData.hasSources()) {
            return;
        }

        log.debug("No source found for transaction {} with trust score {}. Retrying ...", transactionData.getHash(), transactionData.getSenderTrustScore());
        int retryTimes = 200 / (transactionData.getRoundedSenderTrustScore() + 1);
        while (!transactionData.hasSources() && retryTimes > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();

            }
            retryTimes--;
            clusterService.selectSources(transactionData);
            if (transactionData.hasSources()) {
                return;
            }
        }

        while (!transactionData.hasSources()) {
            log.debug("Waiting 2 seconds for new zero spend transaction to be added to available sources for transaction {}", transactionData.getHash());
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            clusterService.selectSources(transactionData);
        }
    }

    private void validateTransaction(TransactionData transactionData) {
        if (!validateMultiDag(transactionData)) {
            log.error("Multi DAG validation failed for transaction {}", transactionData.getHash());
            throw new TransactionValidationException(MULTI_DAG_IS_NOT_SUPPORTED);
        }

        if (!validationService.validateTransactionDataIntegrity(transactionData)) {
            log.error("Data Integrity validation failed for transaction {}", transactionData.getHash());
            throw new TransactionValidationException(AUTHENTICATION_FAILED_MESSAGE);
        }

        if (Boolean.FALSE.equals(validationService.validateFullNodeFeeDataIntegrity(transactionData))) {
            log.error("Invalid fullnode fee data for transaction {}", transactionData.getHash());
            throw new TransactionValidationException(INVALID_FULL_NODE_FEE);
        }
        if (!validationService.validateTransactionTimeFields(transactionData)) {
            log.error("Invalid transaction time field for transaction {}", transactionData.getHash());
            throw new TransactionValidationException(String.format(INVALID_TRANSACTION_TIME_FIELD, Instant.now()));
        }

        if (!validationService.validateTransactionTrustScore(transactionData)) {
            log.error("Invalid sender trust score for transaction {}", transactionData.getHash());
            throw new TransactionValidationException(INVALID_TRUST_SCORE_MESSAGE);
        }

        if (!validationService.validateBalancesAndAddToPreBalance(transactionData)) {
            log.error("Balance and Pre balance check failed for transaction {}", transactionData.getHash());
            throw new TransactionValidationException(INSUFFICIENT_FUNDS_MESSAGE);
        }

        if (transactionData.getType().equals(TransactionType.TokenGeneration)
                && !validationService.validateCurrencyUniquenessAndAddUnconfirmedRecord(transactionData)) {
            log.error("Token uniqueness check failed: {}", transactionData.getHash());
            throw new TransactionValidationException(NOT_UNIQUE_TOKEN_GENERATION_TRANSACTION);
        }

        if (transactionData.getType().equals(TransactionType.TokenMinting)
                && !validationService.validateTokenMintingAndAddToAllocatedAmount(transactionData)) {
            log.error("Minting balance check failed: {}", transactionData.getHash());
            throw new TransactionValidationException(INSUFFICIENT_MINTING_FUNDS_MESSAGE);
        }

        if (transactionData.getType().equals(TransactionType.EventHardFork)
                && !validationService.validateEventHardFork(transactionData)) {
            log.error("Hard Fork Event check failed: {}", transactionData.getHash());
            throw new TransactionValidationException(EVENT_HARD_FORK_ERROR);
        }

    }

    @Override
    public void getTransactions(GetTransactionsRequest getTransactionsRequest, HttpServletResponse response) {
        try {
            List<Hash> transactionHashes = getTransactionsRequest.getTransactionHashes();
            boolean isExtended = getTransactionsRequest.isExtended();
            boolean includeRuntimeTrustScore = getTransactionsRequest.isIncludeRuntimeTrustScore();
            PrintWriter output = response.getWriter();
            chunkService.startOfChunk(output);
            AtomicBoolean firstTransactionSent = new AtomicBoolean(false);

            transactionHashes.forEach(transactionHash ->
                    sendTransactionResponse(transactionHash, firstTransactionSent, output, isExtended, includeRuntimeTrustScore)
            );

            chunkService.endOfChunk(output);
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
        }
    }

    @Override
    public ResponseEntity<IResponse> getAddressTransactions(Hash addressHash) {
        List<TransactionResponseData> transactionsDataList = new ArrayList<>();
        AddressTransactionsHistory addressTransactionsHistory = addressTransactionsHistories.getByHash(addressHash);

        try {
            if (addressTransactionsHistory == null) {
                return ResponseEntity.status(HttpStatus.OK).body(new GetAddressTransactionHistoryResponse(transactionsDataList, 0));
            }

            Set<Hash> transactionsHash = addressTransactionsHistory.getTransactionsHistory();
            for (Hash transactionHash : transactionsHash) {
                if (MemoryUtils.getPercentageUsed() >= javaProcessMemoryLimit) {
                    log.warn("Not all transactions for {} in response of getAddressTransactions, used memory {} , limit {}%, total txs {}, sent txs {}",
                            addressHash, MemoryUtils.getPercentageUsedFormatted(), javaProcessMemoryLimit, transactionsHash.size(), transactionsDataList.size());
                    log.debug(MemoryUtils.debugInfo());
                    break;
                }
                TransactionData transactionData = transactions.getByHash(transactionHash);
                transactionsDataList.add(new TransactionResponseData(transactionData));
            }

            return ResponseEntity.status(HttpStatus.OK).body(new GetAddressTransactionHistoryResponse(transactionsDataList, transactionsHash.size()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(
                            ADDRESS_TRANSACTIONS_SERVER_ERROR,
                            STATUS_ERROR));
        }
    }

    @Override
    public void getAddressTransactionBatch(GetAddressTransactionBatchRequest getAddressTransactionBatchRequest, HttpServletResponse response, boolean reduced) {
        try {
            List<Hash> addressHashList = getAddressTransactionBatchRequest.getAddresses();
            boolean extended = getAddressTransactionBatchRequest.isExtended();
            boolean isIncludeRuntimeTrustScore = getAddressTransactionBatchRequest.isIncludeRuntimeTrustScore();
            PrintWriter output = response.getWriter();
            chunkService.startOfChunk(output);

            AtomicBoolean firstTransactionSent = new AtomicBoolean(false);
            addressHashList.forEach(addressHash -> {
                AddressTransactionsHistory addressTransactionsHistory = addressTransactionsHistories.getByHash(addressHash);
                if (addressTransactionsHistory != null) {
                    addressTransactionsHistory.getTransactionsHistory().forEach(transactionHash ->
                            sendTransactionResponse(transactionHash, firstTransactionSent, output, addressHash, reduced, extended, isIncludeRuntimeTrustScore)
                    );
                }
            });
            chunkService.endOfChunk(output);
        } catch (Exception e) {
            log.error("Error sending address transaction batch");
            log.error(e.getMessage());
        }
    }

    @Override
    public void getAddressTransactionBatchByTimestamp(GetAddressTransactionBatchByTimestampRequest getAddressTransactionBatchByTimestampRequest, HttpServletResponse response, boolean reduced) {
        try {
            PrintWriter output = response.getWriter();
            chunkService.startOfChunk(output);

            Instant startTime = getAddressTransactionBatchByTimestampRequest.getStartTime();
            Instant endTime = getAddressTransactionBatchByTimestampRequest.getEndTime();
            Instant now = Instant.now();

            if (startTime == null || (!startTime.isAfter(now) && (endTime == null || !startTime.isAfter(endTime)))) {
                sendAddressTransactionBatchByAttachment(getAddressTransactionBatchByTimestampRequest, reduced, output);
            }
            chunkService.endOfChunk(output);
        } catch (Exception e) {
            log.error("Error sending address transaction batch by timestamp");
            log.error(e.getMessage());
        }
    }

    private void sendAddressTransactionBatchByAttachment(GetAddressTransactionBatchByTimestampRequest getAddressTransactionBatchByTimestampRequest, boolean reduced, PrintWriter output) {
        Set<Hash> addressHashSet = getAddressTransactionBatchByTimestampRequest.getAddresses();
        Instant startTime = getAddressTransactionBatchByTimestampRequest.getStartTime();
        Instant endTime = getAddressTransactionBatchByTimestampRequest.getEndTime();
        Integer limit = getAddressTransactionBatchByTimestampRequest.getLimit();
        TimeOrder order = getAddressTransactionBatchByTimestampRequest.getOrder();
        boolean isIncludeRuntimeTrustScore = getAddressTransactionBatchByTimestampRequest.isIncludeRuntimeTrustScore();

        AtomicBoolean firstTransactionSent = new AtomicBoolean(false);
        addressHashSet.forEach(addressHash -> {
            NavigableMap<Instant, Set<Hash>> transactionsHistoryByAttachment = addressToTransactionsByAttachmentMap.get(addressHash);
            if (transactionsHistoryByAttachment != null) {
                Instant from = startTime;
                Instant to = endTime;
                if (from == null) {
                    from = transactionsHistoryByAttachment.firstKey();
                }
                if (to == null) {
                    to = transactionsHistoryByAttachment.lastKey();
                }
                if (!from.isAfter(to)) {
                    NavigableMap<Instant, Set<Hash>> transactionsHistoryByAttachmentSubMap = getTransactionHistoryByAttachmentSubMap(transactionsHistoryByAttachment, from, to, order);
                    sendAddressTransactionsByAttachmentResponse(addressHash, transactionsHistoryByAttachmentSubMap, limit, reduced, firstTransactionSent, output, isIncludeRuntimeTrustScore);
                }
            }
        });
    }

    private NavigableMap<Instant, Set<Hash>> getTransactionHistoryByAttachmentSubMap(NavigableMap<Instant, Set<Hash>> transactionsHistoryByAttachment, Instant from, Instant to, TimeOrder order) {
        NavigableMap<Instant, Set<Hash>> transactionsHistoryByAttachmentSubMap = transactionsHistoryByAttachment.subMap(from, true, to, true);
        if (order != null && order.equals(TimeOrder.DESC)) {
            transactionsHistoryByAttachmentSubMap = transactionsHistoryByAttachmentSubMap.descendingMap();
        }
        return transactionsHistoryByAttachmentSubMap;
    }

    private void sendAddressTransactionsByAttachmentResponse(Hash addressHash, NavigableMap<Instant, Set<Hash>> transactionsHistoryByAttachmentSubMap, Integer limit, boolean reduced, AtomicBoolean firstTransactionSent, PrintWriter output, boolean includeRuntimeTrustScore) {
        int sentTxNumber = 0;
        for (Set<Hash> transactionHashSet : transactionsHistoryByAttachmentSubMap.values()) {
            boolean maxLimitReached = false;
            for (Hash transactionHash : transactionHashSet) {
                sendTransactionResponse(transactionHash, firstTransactionSent, output, addressHash, reduced, false, includeRuntimeTrustScore);
                sentTxNumber++;
                if (limit != null && sentTxNumber == limit) {
                    maxLimitReached = true;
                    break;
                }
            }
            if (maxLimitReached) {
                break;
            }
        }
    }

    @Override
    public void getAddressTransactionBatchByDate(GetAddressTransactionBatchByDateRequest getAddressTransactionBatchByDateRequest, HttpServletResponse response, boolean reduced) {
        try {
            Set<Hash> addressHashSet = getAddressTransactionBatchByDateRequest.getAddresses();
            LocalDate startDate = getAddressTransactionBatchByDateRequest.getStartDate();
            LocalDate endDate = getAddressTransactionBatchByDateRequest.getEndDate();
            Integer limit = getAddressTransactionBatchByDateRequest.getLimit();
            TimeOrder order = getAddressTransactionBatchByDateRequest.getOrder();
            boolean isIncludeRuntimeTrustScore = getAddressTransactionBatchByDateRequest.isIncludeRuntimeTrustScore();

            Instant startTime = null;
            Instant endTime = null;
            if (startDate != null) {
                startTime = startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
            }
            if (endDate != null) {
                endTime = endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            }

            getAddressTransactionBatchByTimestamp(new GetAddressTransactionBatchByTimestampRequest(addressHashSet, startTime, endTime, limit, order, isIncludeRuntimeTrustScore), response, reduced);
        } catch (Exception e) {
            log.error("Error sending date range address transaction batch by date");
            log.error(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<IResponse> getLastTransactions() {
        List<TransactionData> transactionsDataList = new ArrayList<>();
        Iterator<ExplorerTransactionData> iterator = explorerIndexedTransactionSet.descendingIterator();
        int count = 0;

        while (count < EXPLORER_LAST_TRANSACTIONS_NUMBER && iterator.hasNext()) {
            ExplorerTransactionData explorerTransactionData = iterator.next();
            transactionsDataList.add(transactions.getByHash(explorerTransactionData.getTransactionHash()));
            count++;
        }

        try {
            return ResponseEntity.status(HttpStatus.OK).body(new GetTransactionsResponse(transactionsDataList) {
            });
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(
                            ADDRESS_TRANSACTIONS_SERVER_ERROR,
                            STATUS_ERROR));
        }
    }

    @Override
    public ResponseEntity<IResponse> getTotalTransactions() {
        return ResponseEntity.ok(new GetTotalTransactionsResponse(explorerIndexedTransactionSet.size()));
    }

    @Override
    public ResponseEntity<IResponse> getTransactionsByPage(int page) {
        int totalTransactionsNumber = explorerIndexedTransactionSet.size();
        int index = totalTransactionsNumber - (page - 1) * EXPLORER_TRANSACTION_NUMBER_BY_PAGE;
        if (index < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(EXPLORER_TRANSACTION_PAGE_ERROR, STATUS_ERROR));
        }
        List<TransactionData> transactionDataList = new ArrayList<>();
        int endOfIndex = index - EXPLORER_TRANSACTION_NUMBER_BY_PAGE;
        while (index > endOfIndex && index >= 0) {
            TransactionData transactionData = transactions.getByHash(explorerIndexedTransactionSet.exact(index).getTransactionHash());
            transactionDataList.add(transactionData);
            index--;
        }
        return ResponseEntity.ok(new GetTransactionsResponse(transactionDataList));
    }

    @Override
    public ResponseEntity<IResponse> getTransactionDetails(Hash transactionHash, boolean extended) {
        try {
            TransactionData transactionData = transactions.getByHash(transactionHash);
            if (transactionData == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(TRANSACTION_DOESNT_EXIST_MESSAGE, STATUS_ERROR));
            }
            TransactionResponseData transactionResponseData = extended ? new ExtendedTransactionResponseData(transactionData) : new TransactionResponseData(transactionData);
            GetTransactionResponse getTransactionResponse = extended ? new GetExtendedTransactionResponse((ExtendedTransactionResponseData) transactionResponseData) : new GetTransactionResponse(transactionResponseData);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(getTransactionResponse);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(TRANSACTION_DETAILS_SERVER_ERROR, STATUS_ERROR));
        }
    }

    @Override
    public void addDataToMemory(TransactionData transactionData) {
        try {
            explorerIndexQueue.put(new ExplorerTransactionData(transactionData));
            if (!transactionData.getType().equals(TransactionType.ZeroSpend)) {
                addressTransactionsByAttachmentQueue.put(transactionData);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void removeDataFromMemory(TransactionData transactionData) {
        explorerIndexedTransactionSet.remove(new ExplorerTransactionData(transactionData));
        webSocketSender.notifyTotalTransactionsChange(explorerIndexedTransactionSet.size());
        removeAddressTransactionsByAttachment(transactionData);
    }

    @Override
    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
        webSocketSender.notifyTransactionHistoryChange(transactionData, TransactionStatus.ATTACHED_TO_DAG);
        addDataToMemory(transactionData);
    }

    private void updateExplorerIndex() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ExplorerTransactionData explorerTransactionData = explorerIndexQueue.take();
                explorerIndexedTransactionSet.add(explorerTransactionData);
                webSocketSender.notifyTotalTransactionsChange(explorerIndexedTransactionSet.size());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void updateAddressTransactionsByAttachment() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                TransactionData transactionData = addressTransactionsByAttachmentQueue.take();
                transactionData.getBaseTransactions().forEach(baseTransactionData -> {
                    NavigableMap<Instant, Set<Hash>> transactionHashesByAttachmentMap = addressToTransactionsByAttachmentMap.getOrDefault(baseTransactionData.getAddressHash(), new ConcurrentSkipListMap<>());
                    Set<Hash> transactionHashSet = transactionHashesByAttachmentMap.getOrDefault(transactionData.getAttachmentTime(), Sets.newConcurrentHashSet());
                    transactionHashSet.add(transactionData.getHash());
                    transactionHashesByAttachmentMap.put(transactionData.getAttachmentTime(), transactionHashSet);
                    addressToTransactionsByAttachmentMap.put(baseTransactionData.getAddressHash(), transactionHashesByAttachmentMap);
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void removeAddressTransactionsByAttachment(TransactionData transactionData) {
        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            NavigableMap<Instant, Set<Hash>> transactionHashesByAttachmentMap = addressToTransactionsByAttachmentMap.getOrDefault(baseTransactionData.getAddressHash(), new ConcurrentSkipListMap<>());
            Set<Hash> transactionHashSet = transactionHashesByAttachmentMap.getOrDefault(transactionData.getAttachmentTime(), Sets.newConcurrentHashSet());
            transactionHashSet.remove(transactionData.getHash());
            transactionHashesByAttachmentMap.put(transactionData.getAttachmentTime(), transactionHashSet);
            addressToTransactionsByAttachmentMap.put(baseTransactionData.getAddressHash(), transactionHashesByAttachmentMap);
        });
    }

    @Override
    public void removeTransactionHashFromUnconfirmed(TransactionData transactionData) {
        transactionPropagationCheckService.removeTransactionHashFromUnconfirmed(transactionData.getHash());
    }

    @Override
    public void handlePropagatedRejectedTransaction(RejectedTransactionData rejectedTransactionData) {
        if (!nodeEventService.eventHappened(Event.TRUST_SCORE_CONSENSUS)) {
            log.error("Error encountered during the handling of rejected transaction data, event TRUST_SCORE_CONSENSUS didn't happen");
            return;
        }
        Hash rejectedTransactionHash = rejectedTransactionData.getHash();
        if (rejectedTransactionHash == null) {
            log.error("Error encountered during the handling of rejected transaction data, hash is null");
            return;
        }
        try {
            transactionReadWriteLock.writeLock().lock();
            synchronized (transactionLockData.addLockToLockMap(rejectedTransactionHash)) {
                if (!nodeTransactionHelper.isTransactionHashExists(rejectedTransactionHash)) {
                    return;
                }
                if (!validationService.validatePropagatedRejectedTransactionDataIntegrity(rejectedTransactionData)) {
                    log.error("Data Integrity validation failed for rejected transaction: {}", rejectedTransactionHash);
                    return;
                }

                rejectTransaction(rejectedTransactionData);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        } finally {
            transactionLockData.removeLockFromLocksMap(rejectedTransactionHash);
            transactionReadWriteLock.writeLock().unlock();
        }
    }

    private void rejectTransaction(RejectedTransactionData rejectedTransactionData) {
        Hash rejectedTransactionDataHash = rejectedTransactionData.getHash();
        TransactionData transactionData = transactions.getByHash(rejectedTransactionDataHash);
        if (transactionData == null) {
            throw new TransactionException("No TransactionData found for rejected transaction hash: " + rejectedTransactionDataHash);
        }

        transactionData.getChildrenTransactionHashes().forEach(hash ->
                rejectTransaction(new RejectedTransactionData(transactions.getByHash(hash), REJECTED_PARENT)));

        log.debug("Starting to remove the rejected transaction {}", rejectedTransactionDataHash);
        removeDataFromMemory(transactionData);
        nodeTransactionHelper.continueHandleRejectedTransaction(transactionData);
        webSocketSender.notifyTransactionHistoryChange(transactionData, TransactionStatus.REJECTED);
        rejectedTransactions.put(rejectedTransactionData);
        addAddressToRejectedTransactionsMap(rejectedTransactionData);
    }

    private void addAddressToRejectedTransactionsMap(RejectedTransactionData rejectedTransactionData) {
        rejectedTransactionData.getTransactionData().getBaseTransactions().forEach(baseTransactionData -> {
            Set<Hash> transactionHashSet = addressToRejectedTransactionsMap.getOrDefault(baseTransactionData.getAddressHash(), Sets.newConcurrentHashSet());
            transactionHashSet.add(rejectedTransactionData.getHash());
            addressToRejectedTransactionsMap.put(baseTransactionData.getAddressHash(), transactionHashSet);
        });
    }

    private void removeAddressFromRejectedTransactionsMap(RejectedTransactionData rejectedTransactionData) {
        rejectedTransactionData.getTransactionData().getBaseTransactions().forEach(baseTransactionData -> {
            Set<Hash> transactionHashSet = addressToRejectedTransactionsMap.getOrDefault(baseTransactionData.getAddressHash(), Sets.newConcurrentHashSet());
            transactionHashSet.remove(rejectedTransactionData.getHash());
            if (transactionHashSet.isEmpty()) {
                addressToRejectedTransactionsMap.remove(baseTransactionData.getAddressHash());
            } else {
                addressToRejectedTransactionsMap.put(baseTransactionData.getAddressHash(), transactionHashSet);
            }
        });
    }

    private void initAddressToRejectedTransactionsMap() {
        addressToRejectedTransactionsMap = new ConcurrentHashMap<>();
        rejectedTransactions.forEach(this::addAddressToRejectedTransactionsMap);
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 86400000)
    private void clearRejectedTransactions() {
        rejectedTransactions.forEach(rejectedTransaction -> {
                    if (rejectedTransaction != null && (Instant.now().getEpochSecond() - rejectedTransaction.getRejectionTime().getEpochSecond() > REJECTED_TRANSACTIONS_TTL)) {
                        log.debug("removing rejected transaction due to TTL. hash: {}, rejection time: {}, reason: {}",
                                rejectedTransaction.getHash(), rejectedTransaction.getRejectionTime(), rejectedTransaction.getRejectionReasonDescription());
                        rejectedTransactions.delete(rejectedTransaction);
                        removeAddressFromRejectedTransactionsMap(rejectedTransaction);
                    }
                }
        );
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 5000)
    public void monitorCurrentAddTransaction() {
        int currentAddTransactionForMonitoring = currentlyAddTransaction.get();
        if (currentAddTransactionForMonitoring > 0) {
            log.info("Current add tx number: {}", currentAddTransactionForMonitoring);
        }
    }

    @Override
    public void getAddressRejectedTransactionBatch(GetAddressTransactionBatchRequest getAddressTransactionBatchRequest, HttpServletResponse response) {
        try {
            List<Hash> addressHashList = getAddressTransactionBatchRequest.getAddresses();
            PrintWriter output = response.getWriter();
            chunkService.startOfChunk(output);
            AtomicBoolean firstTransactionSent = new AtomicBoolean(false);
            addressHashList.forEach(addressHash -> {
                Set<Hash> transactionHashSet = addressToRejectedTransactionsMap.getOrDefault(addressHash, Sets.newConcurrentHashSet());
                transactionHashSet.forEach(rejectedTransactionHash -> {
                    RejectedTransactionData rejectedTransactionData = rejectedTransactions.getByHash(rejectedTransactionHash);
                    ITransactionResponseData transactionResponseData = new RejectedTransactionResponseData(rejectedTransactionData);
                    if (firstTransactionSent.get()) {
                        chunkService.sendChunk(",", output);
                    } else {
                        firstTransactionSent.set(true);
                    }
                    chunkService.sendChunk(new CustomGson().getInstance().toJson(transactionResponseData), output);
                });
            });
            chunkService.endOfChunk(output);
        } catch (Exception e) {
            log.error("Error sending address rejected transaction batch");
            log.error(e.getMessage());
        }
    }
}
