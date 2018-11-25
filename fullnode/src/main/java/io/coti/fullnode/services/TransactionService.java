package io.coti.fullnode.services;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.AddressTransactionsHistory;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.exceptions.TransactionException;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.data.TransactionResponseData;
import io.coti.basenode.http.data.TransactionStatus;
import io.coti.basenode.model.AddressTransactionsHistories;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.basenode.services.interfaces.*;

import io.coti.fullnode.http.AddTransactionRequest;
import io.coti.fullnode.http.AddTransactionResponse;
import io.coti.fullnode.http.GetAddressTransactionHistoryResponse;
import io.coti.fullnode.http.GetTransactionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;

@Slf4j
@Service
public class TransactionService extends BaseNodeTransactionService {

    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private TransactionCrypto transactionCrypto;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private IClusterService clusterService;

    @Autowired
    private ISender sender;
    @Autowired
    private AddressTransactionsHistories addressTransactionHistories;
    @Autowired
    private Transactions transactions;
    @Autowired
    private WebSocketSender webSocketSender;
    @Autowired
    private INetworkDetailsService networkDetailsService;

    @Autowired
    private PotService potService;

    public ResponseEntity<Response> addNewTransaction(AddTransactionRequest request) {
        TransactionData transactionData =
                new TransactionData(
                        request.baseTransactions,
                        request.hash,
                        request.transactionDescription,
                        request.trustScoreResults,
                        request.createTime,
                        request.senderHash);
        try {
            log.debug("New transaction request is being processed. Transaction Hash = {}", request.hash);
            transactionCrypto.signMessage(transactionData);
            if (transactionHelper.isTransactionExists(transactionData)) {
                log.debug("Received existing transaction: {}", transactionData.getHash());
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new AddTransactionResponse(
                                STATUS_ERROR,
                                TRANSACTION_ALREADY_EXIST_MESSAGE));
            }
            transactionHelper.startHandleTransaction(transactionData);
            if (!validationService.validateTransactionDataIntegrity(transactionData)) {
                log.error("Data Integrity validation failed: {}", transactionData.getHash());
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new AddTransactionResponse(
                                STATUS_ERROR,
                                AUTHENTICATION_FAILED_MESSAGE));
            }

            if (!validationService.validateBaseTransactionAmounts(transactionData)) {
                log.error("Illegal base transaction amounts: {}", transactionData.getHash());
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new AddTransactionResponse(
                                STATUS_ERROR,
                                ILLEGAL_TRANSACTION_MESSAGE));
            }
            if (!validationService.validateTransactionTrustScore(transactionData)) {
                log.error("Invalid sender trust score: {}", transactionData.getHash());
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new AddTransactionResponse(
                                STATUS_ERROR,
                                INVALID_TRUST_SCORE_MESSAGE));
            }

            if (!validationService.validateBalancesAndAddToPreBalance(transactionData)) {
                log.error("Balance and Pre balance check failed: {}", transactionData.getHash());
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new AddTransactionResponse(
                                STATUS_ERROR,
                                INSUFFICIENT_FUNDS_MESSAGE));
            }

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
                //TODO: Implement an invalidation mechanism for TestNet.
            }

            transactionData.setPowStartTime(new Date());
            // ############   POT   ###########
            potService.potAction(transactionData);
            // ################################
            transactionData.setPowEndTime(new Date());

            transactionData.setAttachmentTime(new Date());

            transactionHelper.attachTransactionToCluster(transactionData);
            transactionHelper.setTransactionStateToSaved(transactionData);
            webSocketSender.notifyTransactionHistoryChange(transactionData, TransactionStatus.ATTACHED_TO_DAG);
            final TransactionData finalTransactionData = transactionData;
            networkDetailsService.getNetworkDetails().getDspNetworkNodesMap().forEach ((hash, networkNode) ->
                    sender.send(finalTransactionData, networkNode.getReceivingFullAddress())
            );
            transactionHelper.setTransactionStateToFinished(transactionData);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new AddTransactionResponse(
                            STATUS_SUCCESS,
                            TRANSACTION_CREATED_MESSAGE));

        } catch (Exception ex) {
            log.error("Exception while adding transaction: {}", transactionData.getHash(), ex);
            throw new TransactionException(ex);
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
                e.printStackTrace();

            }
            retryTimes--;
            clusterService.selectSources(transactionData);
            if (transactionData.hasSources()) {
                return;
            }
        }
        //TODO: ZeroSpend source starvation already implemented. ZeroSpend source creation by request will be implemented for TestNet.
/*      ZeroSpendTransactionRequest zeroSpendTransactionRequest = new ZeroSpendTransactionRequest();
        zeroSpendTransactionRequest.setTransactionData(transactionData);
        receivingServerAddresses.forEach(address -> sender.send(zeroSpendTransactionRequest, address)); */

        while (!transactionData.hasSources()) {
            log.debug("Waiting 2 seconds for new zero spend transaction to be added to available sources for transaction {}", transactionData.getHash());
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                log.error("Errors when sleeping: ", e);
            }
            clusterService.selectSources(transactionData);
        }
    }

    public ResponseEntity<BaseResponse> getAddressTransactions(Hash addressHash) {
        List<TransactionData> transactionsDataList = new Vector<>();
        AddressTransactionsHistory addressTransactionsHistory = addressTransactionHistories.getByHash(addressHash);

        if (addressTransactionsHistory == null) {
            return ResponseEntity.status(HttpStatus.OK).body(new GetAddressTransactionHistoryResponse(transactionsDataList));
        }

        for (Hash transactionHash : addressTransactionsHistory.getTransactionsHistory()) {
            TransactionData transactionData = transactions.getByHash(transactionHash);
            transactionsDataList.add(transactionData);

        }
        return ResponseEntity.status(HttpStatus.OK).body(new GetAddressTransactionHistoryResponse(transactionsDataList));
    }

    public ResponseEntity<BaseResponse> getTransactionDetails(Hash transactionHash) {
        TransactionData transactionData = transactions.getByHash(transactionHash);
        if (transactionData == null)
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new GetTransactionResponse(
                            STATUS_ERROR,
                            TRANSACTION_DOESNT_EXIST_MESSAGE));
        TransactionResponseData transactionResponseData = new TransactionResponseData(transactionData);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GetTransactionResponse(transactionResponseData));
    }

    @Override
    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
        webSocketSender.notifyTransactionHistoryChange(transactionData, TransactionStatus.ATTACHED_TO_DAG);
    }

}