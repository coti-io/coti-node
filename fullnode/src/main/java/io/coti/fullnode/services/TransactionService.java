package io.coti.fullnode.services;

import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.AddressTransactionsHistory;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.exceptions.TransactionException;
import io.coti.basenode.exceptions.TransactionValidationException;
import io.coti.basenode.http.CustomGson;
import io.coti.basenode.http.GetTransactionsResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.data.ReducedTransactionResponseData;
import io.coti.basenode.http.data.TransactionResponseData;
import io.coti.basenode.http.data.TransactionStatus;
import io.coti.basenode.http.data.interfaces.ITransactionResponseData;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.AddressTransactionsHistories;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.basenode.services.interfaces.IClusterService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.fullnode.data.ExplorerIndexData;
import io.coti.fullnode.http.*;
import io.coti.fullnode.model.ExplorerIndexes;
import io.coti.fullnode.websocket.WebSocketSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;

@Slf4j
@Service
public class TransactionService extends BaseNodeTransactionService {

    private static final int EXPLORER_LAST_TRANSACTIONS_AMOUNT = 20;

    private AtomicLong explorerIndex;
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private TransactionCrypto transactionCrypto;
    @Autowired
    private ValidationService validationService;
    @Autowired
    private IClusterService clusterService;
    @Autowired
    private ExplorerIndexes explorerIndexes;
    @Autowired
    private AddressTransactionsHistories addressTransactionHistories;
    @Autowired
    private Transactions transactions;
    @Autowired
    private WebSocketSender webSocketSender;
    @Autowired
    private INetworkService networkService;
    @Autowired
    private PotService potService;

    @Override
    public void init() {
        explorerIndex = new AtomicLong(0);
        super.init();
    }

    public ResponseEntity<Response> addNewTransaction(AddTransactionRequest request) {
        TransactionData transactionData =
                new TransactionData(
                        request.baseTransactions,
                        request.hash,
                        request.transactionDescription,
                        request.trustScoreResults,
                        request.createTime,
                        request.senderHash,
                        request.senderSignature,
                        request.type);
        try {
            log.debug("New transaction request is being processed. Transaction Hash = {}", request.hash);

            if (transactionHelper.isTransactionExists(transactionData)) {
                log.debug("Received existing transaction: {}", transactionData.getHash());
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new AddTransactionResponse(
                                STATUS_ERROR,
                                TRANSACTION_ALREADY_EXIST_MESSAGE));
            }
            transactionHelper.startHandleTransaction(transactionData);

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
                            .body(new AddTransactionResponse(
                                    STATUS_ERROR,
                                    TRANSACTION_SOURCE_NOT_FOUND));
                }
            }
            if (!validationService.validateSource(transactionData.getLeftParentHash()) ||
                    !validationService.validateSource(transactionData.getRightParentHash())) {
                log.debug("Could not validate transaction source");
            }

            // ############   POT   ###########
            try {
                potService.potAction(transactionData);
            } catch (IllegalArgumentException e) {
                log.error("Error at POT: {} , Transaction: {}", e.getMessage(), transactionData.getHash());
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new AddTransactionResponse(
                                STATUS_ERROR,
                                e.getMessage()));
            }
            // ################################

            transactionData.setAttachmentTime(Instant.now());
            transactionCrypto.signMessage(transactionData);
            transactionHelper.attachTransactionToCluster(transactionData);
            transactionHelper.setTransactionStateToSaved(transactionData);
            webSocketSender.notifyTransactionHistoryChange(transactionData, TransactionStatus.ATTACHED_TO_DAG);
            addToExplorerIndexes(transactionData);
            final TransactionData finalTransactionData = transactionData;
            ((NetworkService) networkService).sendDataToConnectedDspNodes(finalTransactionData);
            transactionHelper.setTransactionStateToFinished(transactionData);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new AddTransactionResponse(
                            STATUS_SUCCESS,
                            TRANSACTION_CREATED_MESSAGE));

        } catch (TransactionValidationException e) {
            log.error("Transaction validation failed: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AddTransactionResponse(
                            STATUS_ERROR,
                            e.getMessage()));
        } catch (Exception e) {
            log.error("Exception while adding transaction: {}", transactionData.getHash());
            throw new TransactionException(e);
        } finally {
            transactionHelper.endHandleTransaction(transactionData);
        }
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

        if (!validationService.validateTransactionDataIntegrity(transactionData)) {
            log.error("Data Integrity validation failed for transaction {}", transactionData.getHash());
            throw new TransactionValidationException(AUTHENTICATION_FAILED_MESSAGE);
        }

        if (!validationService.validateFullNodeFeeDataIntegrity(transactionData)) {
            log.error("Invalid fullnode fee data for transaction {}", transactionData.getHash());
            throw new TransactionValidationException(INVALID_FULL_NODE_FEE);
        }
        if (!validationService.validateBaseTransactionAmounts(transactionData)) {
            log.error("Illegal base transaction amounts for transaction {}", transactionData.getHash());
            throw new TransactionValidationException(ILLEGAL_BASE_TRANSACTIONS_AMOUNT);
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

    }

    public void getTransactions(GetTransactionsRequest getTransactionsRequest, HttpServletResponse response) {
        try {
            List<Hash> transactionHashes = getTransactionsRequest.getTransactionHashes();
            PrintWriter output = response.getWriter();
            output.write("[");
            output.flush();
            AtomicBoolean firstTransactionSent = new AtomicBoolean(false);
            transactionHashes.forEach(transactionHash ->
                    sendTransactionResponse(transactionHash, firstTransactionSent, output)
            );

            output.write("]");
            output.flush();
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
        }
    }

    public ResponseEntity<IResponse> getAddressTransactions(Hash addressHash) {
        List<TransactionData> transactionsDataList = new ArrayList<>();
        AddressTransactionsHistory addressTransactionsHistory = addressTransactionHistories.getByHash(addressHash);

        try {
            if (addressTransactionsHistory == null) {
                return ResponseEntity.status(HttpStatus.OK).body(new GetAddressTransactionHistoryResponse(transactionsDataList));
            }
            addressTransactionsHistory.getTransactionsHistory().forEach(transactionHash -> {
                TransactionData transactionData = transactions.getByHash(transactionHash);
                transactionsDataList.add(transactionData);
            });
            return ResponseEntity.status(HttpStatus.OK).body(new GetAddressTransactionHistoryResponse(transactionsDataList));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(
                            ADDRESS_TRANSACTIONS_SERVER_ERROR,
                            STATUS_ERROR));
        }
    }

    public void getAddressTransactionBatch(GetAddressTransactionBatchRequest getAddressTransactionBatchRequest, HttpServletResponse response, boolean reduced) {
        try {
            List<Hash> addressHashList = getAddressTransactionBatchRequest.getAddresses();
            PrintWriter output = response.getWriter();
            output.write("[");
            output.flush();

            AtomicBoolean firstTransactionSent = new AtomicBoolean(false);
            addressHashList.forEach(addressHash -> {
                AddressTransactionsHistory addressTransactionsHistory = addressTransactionHistories.getByHash(addressHash);
                if (addressTransactionsHistory != null) {
                    addressTransactionsHistory.getTransactionsHistory().forEach(transactionHash ->
                            sendTransactionResponse(transactionHash, firstTransactionSent, output, addressHash, reduced)
                    );
                }
            });
            output.write("]");
            output.flush();
        } catch (Exception e) {
            log.error("Error sending address transaction batch");
            log.error(e.getMessage());
        }
    }

    private void sendTransactionResponse(Hash transactionHash, AtomicBoolean firstTransactionSent, PrintWriter output) {
        sendTransactionResponse(transactionHash, firstTransactionSent, output, null, false);
    }

    private void sendTransactionResponse(Hash transactionHash, AtomicBoolean firstTransactionSent, PrintWriter output, Hash addressHash, boolean reduced) {
        try {
            TransactionData transactionData = transactions.getByHash(transactionHash);
            if (transactionData != null) {
                ITransactionResponseData transactionResponseData = !reduced ? new TransactionResponseData(transactionData) : new ReducedTransactionResponseData(transactionData, addressHash);
                if (firstTransactionSent.get()) {
                    output.write(",");
                    output.flush();
                } else {
                    firstTransactionSent.set(true);
                }
                output.write(new CustomGson().getInstance().toJson(transactionResponseData));
                output.flush();
            }
        } catch (Exception e) {
            log.error("Error at transaction response data for {}", transactionHash.toString());
            log.error(e.getMessage());
        }
    }

    public ResponseEntity<IResponse> getLastTransactions() {
        List<TransactionData> transactionsDataList = new ArrayList<>();
        ExplorerIndexData explorerIndexData;
        long currentExplorerIndex = explorerIndex.get();

        for (int i = 0; i < EXPLORER_LAST_TRANSACTIONS_AMOUNT; i++) {
            if (currentExplorerIndex - i < 1) {
                break;
            }
            explorerIndexData = explorerIndexes.getByHash(new Hash(currentExplorerIndex - i));
            transactionsDataList.add(transactions.getByHash(explorerIndexData.getTransactionHash()));
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

    public ResponseEntity<IResponse> getTransactionDetails(Hash transactionHash) {
        TransactionData transactionData = transactions.getByHash(transactionHash);
        if (transactionData == null)
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(
                            TRANSACTION_DOESNT_EXIST_MESSAGE,
                            STATUS_ERROR));
        try {
            TransactionResponseData transactionResponseData = new TransactionResponseData(transactionData);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new GetTransactionResponse(transactionResponseData));
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(
                            TRANSACTION_DETAILS_SERVER_ERROR,
                            STATUS_ERROR));
        }
    }

    private long incrementAndGetExplorerIndex() {
        return explorerIndex.incrementAndGet();
    }

    @Override
    public void addToExplorerIndexes(TransactionData transactionData) {
        explorerIndexes.put(new ExplorerIndexData(incrementAndGetExplorerIndex(), transactionData.getHash()));
    }


    @Override
    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
        webSocketSender.notifyTransactionHistoryChange(transactionData, TransactionStatus.ATTACHED_TO_DAG);
        addToExplorerIndexes(transactionData);
    }
}