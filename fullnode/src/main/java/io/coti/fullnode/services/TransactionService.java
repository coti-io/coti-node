package io.coti.fullnode.services;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.AddressTransactionsHistory;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.exceptions.TransactionException;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.data.TransactionResponseData;
import io.coti.basenode.http.data.TransactionStatus;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.model.AddressTransactionsHistories;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.basenode.services.interfaces.IClusterService;
import io.coti.basenode.services.interfaces.IClusterStampService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.basenode.services.interfaces.IValidationService;
import io.coti.fullnode.http.AddTransactionRequest;
import io.coti.fullnode.http.AddTransactionResponse;
import io.coti.fullnode.http.GetAddressTransactionHistoryResponse;
import io.coti.fullnode.http.GetTransactionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.*;

@Slf4j
@Service
public class TransactionService extends BaseNodeTransactionService {
    @Value("#{'${receiving.server.addresses}'.split(',')}")
    private List<String> receivingServerAddresses;
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
    private IClusterStampService clusterStampService;

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
                        request.senderHash,
                        request.type);
        try {
            log.debug("New transaction request is being processed. Transaction Hash = {}", request.hash);
            transactionCrypto.signMessage(transactionData);

            Optional<ResponseEntity<Response>> validationResponseOpt = validateNewTransaction(transactionData);
            if(validationResponseOpt.isPresent()){
                return validationResponseOpt.get();
            }

            //TODO 2/4/2019 astolia:  handle transactions here is case snapshot is in progeress.
            // add them to a different collection with status CLUSTERSTAMP
            if(clusterStampService.getIsClusterStampInProgress()){

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
            receivingServerAddresses.forEach(address -> sender.send(finalTransactionData, address));
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

    private Optional<ResponseEntity<Response>> getValidationResponse(String transactionMessage){
        return Optional.of(ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new AddTransactionResponse(
                        STATUS_ERROR,
                        transactionMessage)));
    }

    private Optional<ResponseEntity<Response>> handleFailedValidation(TransactionData transactionData, String errorMsg, String transactionMessage){
        log.debug(errorMsg, transactionData.getHash());
        return getValidationResponse(transactionMessage);
    }

    private Optional<ResponseEntity<Response>> validateNewTransaction(TransactionData transactionData){
        if (transactionHelper.isTransactionExists(transactionData)) {
            return handleFailedValidation(transactionData, "Received existing transaction: {}", TRANSACTION_ALREADY_EXIST_MESSAGE);
        }

        transactionHelper.startHandleTransaction(transactionData);
        if (!validationService.validateTransactionDataIntegrity(transactionData)) {
            return handleFailedValidation(transactionData, "Data Integrity validation failed: {}", AUTHENTICATION_FAILED_MESSAGE);
        }

        if (!validationService.validateBaseTransactionAmounts(transactionData)) {
            return handleFailedValidation(transactionData, "Illegal base transaction amounts: {}", ILLEGAL_TRANSACTION_MESSAGE);
        }

        if (!validationService.validateTransactionTrustScore(transactionData)) {
            return handleFailedValidation(transactionData, "Invalid sender trust score: {}", INVALID_TRUST_SCORE_MESSAGE);
        }

        if (!validationService.validateBalancesAndAddToPreBalance(transactionData)) {
            return handleFailedValidation(transactionData, "Balance and Pre balance check failed: {}", INSUFFICIENT_FUNDS_MESSAGE);
        }

        return Optional.empty();
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

    public ResponseEntity<IResponse> getAddressTransactions(Hash addressHash) {
        List<TransactionData> transactionsDataList = new ArrayList<>();
        AddressTransactionsHistory addressTransactionsHistory = addressTransactionHistories.getByHash(addressHash);

        try {
            if (addressTransactionsHistory == null) {
                return ResponseEntity.status(HttpStatus.OK).body(new GetAddressTransactionHistoryResponse(transactionsDataList));
            }

            for (Hash transactionHash : addressTransactionsHistory.getTransactionsHistory()) {
                TransactionData transactionData = transactions.getByHash(transactionHash);
                transactionsDataList.add(transactionData);

            }
            return ResponseEntity.status(HttpStatus.OK).body(new GetAddressTransactionHistoryResponse(transactionsDataList));
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

    @Override
    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
        webSocketSender.notifyTransactionHistoryChange(transactionData, TransactionStatus.ATTACHED_TO_DAG);
    }

}