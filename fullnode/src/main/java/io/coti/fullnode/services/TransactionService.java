package io.coti.fullnode.services;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.AddressTransactionsHistory;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.ZeroSpendTransactionRequest;
import io.coti.basenode.exceptions.TransactionException;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.data.TransactionResponseData;
import io.coti.basenode.http.data.TransactionStatus;
import io.coti.basenode.model.AddressTransactionsHistories;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeTransactionService;
import io.coti.basenode.services.interfaces.IClusterService;
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

import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import static io.coti.basenode.http.HttpStringConstants.*;

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
    private PotWorkerService potService;

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
            log.debug("New transaction request is being processed. Transaction Hash= {}", request.hash);
            transactionCrypto.signMessage(transactionData);
            if (!transactionHelper.startHandleTransaction(transactionData)) {
                log.debug("Received existing transaction: {}", transactionData.getHash().toHexString());
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new AddTransactionResponse(
                                STATUS_ERROR,
                                TRANSACTION_ALREADY_EXIST_MESSAGE));
            }

            if (!transactionHelper.validateTransaction(transactionData)) {
                log.error("Data Integrity validation failed: {}", transactionData.getHash().toHexString());
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new AddTransactionResponse(
                                STATUS_ERROR,
                                AUTHENTICATION_FAILED_MESSAGE));
            }

            if (!transactionHelper.isLegalBalance(transactionData.getBaseTransactions())) {
                log.error("Illegal transaction balance: {}", transactionData.getHash().toHexString());
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new AddTransactionResponse(
                                STATUS_ERROR,
                                ILLEGAL_TRANSACTION_MESSAGE));
            }
            if (!transactionHelper.validateTrustScore(transactionData)) {
                log.error("Invalid sender trust score: {}", transactionData.getHash().toHexString());
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new AddTransactionResponse(
                                STATUS_ERROR,
                                INVALID_TRUST_SCORE_MESSAGE));
            }

            if (!transactionHelper.checkBalancesAndAddToPreBalance(transactionData)) {
                log.error("Balance and Pre balance check failed: {}", transactionData.getHash().toHexString());
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new AddTransactionResponse(
                                STATUS_ERROR,
                                INSUFFICIENT_FUNDS_MESSAGE));
            }
            selectSources(transactionData);
            while (transactionData.getLeftParentHash() == null && transactionData.getRightParentHash() == null) {
                log.debug("Could not find sources for transaction: {}. Sending to Zero Spend and retrying in 5 seconds.", transactionData.getHash().toHexString());
                TimeUnit.SECONDS.sleep(5);
                selectSources(transactionData);
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
            log.error("Exception while adding transaction: {}", transactionData.getHash().toHexString(), ex);
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

        log.debug("No sources found for transaction {} with trust score {}", transactionData.getHash().toHexString(), transactionData.getSenderTrustScore());
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

        ZeroSpendTransactionRequest zeroSpendTransactionRequest = new ZeroSpendTransactionRequest();
        zeroSpendTransactionRequest.setTransactionData(transactionData);
        receivingServerAddresses.forEach(address -> sender.send(zeroSpendTransactionRequest, address));
        clusterService.selectSources(transactionData);

        while (!transactionData.hasSources()) {
            log.debug("Waiting 2 seconds for new zero spend transaction to be added to available sources for transaction {}", transactionData.getHash().toHexString());
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                log.error("Errors when sleeping: {}", e);
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
               if (validationService.validatePot(transactionData)) {
                   webSocketSender.notifyTransactionHistoryChange(transactionData, TransactionStatus.ATTACHED_TO_DAG);
               }
    }

}