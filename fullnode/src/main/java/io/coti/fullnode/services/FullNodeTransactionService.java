package io.coti.fullnode.services;

import io.coti.common.communication.interfaces.ISender;
import io.coti.common.crypto.TransactionCrypto;
import io.coti.common.data.AddressTransactionsHistory;
import io.coti.common.data.DspConsensusResult;
import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.exceptions.TransactionException;
import io.coti.common.http.*;
import io.coti.common.http.data.TransactionStatus;
import io.coti.common.model.AddressesTransactionsHistory;
import io.coti.common.model.DbItem;
import io.coti.common.model.Transactions;
import io.coti.common.services.TransactionService;
import io.coti.common.services.LiveView.WebSocketSender;
import io.coti.common.services.interfaces.*;
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

import static io.coti.common.http.HttpStringConstants.*;

@Slf4j
@Service
public class FullNodeTransactionService extends TransactionService {
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
    private IBalanceService balanceService;
    @Autowired
    private IZeroSpendService zeroSpendService;
    @Autowired
    private ISender sender;
    @Autowired
    private AddressesTransactionsHistory addressesTransactionsHistory;
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
            log.debug("New transaction request is being processed. Transaction Hash={}", request.hash);
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
                log.debug("Could not find sources for transaction: {}. Sending to Zero Spend and retrying in 5 seconds.");
                TimeUnit.SECONDS.sleep(5);
                selectSources(transactionData);
            }

            if (!validationService.validateSource(transactionData.getLeftParentHash()) ||
                    !validationService.validateSource(transactionData.getRightParentHash())) {
                log.debug("Could not validate transaction source");
                //TODO: decide what to do here
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

        TransactionData zeroSpendTransaction = zeroSpendService.getZeroSpendTransaction(transactionData.getSenderTrustScore());
        transactionHelper.attachTransactionToCluster(zeroSpendTransaction);
        clusterService.attachToCluster(zeroSpendTransaction);
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
        DbItem<AddressTransactionsHistory> dbAddress = addressesTransactionsHistory.getByHashItem(addressHash);

        if (!dbAddress.isExists)
            return ResponseEntity.status(HttpStatus.OK).body(new GetAddressTransactionHistory(transactionsDataList));

        AddressTransactionsHistory history = dbAddress.item;
        for (Hash transactionHash : history.getTransactionsHistory()) {
            TransactionData transactionData = transactions.getByHash(transactionHash);
            transactionsDataList.add(transactionData);

        }
        return ResponseEntity.status(HttpStatus.OK).body(new GetAddressTransactionHistory(transactionsDataList));
    }

    @Override
    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
                !validationService.validatePot(transactionData)) {
        webSocketSender.notifyTransactionHistoryChange(transactionData, TransactionStatus.ATTACHED_TO_DAG);
    }

    public void handleDspConsensusResult(DspConsensusResult dspConsensusResult) {
        log.debug("Received DspConsensus result for transaction: {}", dspConsensusResult.getHash());
        if (!transactionHelper.handleVoteConclusionResult(dspConsensusResult)) {
            log.error("Illegal Dsp consensus result for transaction: {}", dspConsensusResult.getHash());
        } else {
            balanceService.setDspcToTrue(dspConsensusResult);
        }
    }
}