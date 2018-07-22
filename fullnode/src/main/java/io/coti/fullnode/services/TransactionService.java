package io.coti.fullnode.services;

import io.coti.common.communication.interfaces.ITransactionPropagationSubscriber;
import io.coti.common.communication.interfaces.ITransactionSender;
import io.coti.common.data.AddressTransactionsHistory;
import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.exceptions.TransactionException;
import io.coti.common.http.AddTransactionRequest;
import io.coti.common.http.AddTransactionResponse;
import io.coti.common.http.GetAddressTransactionHistory;
import io.coti.common.http.Response;
import io.coti.common.model.AddressesTransactionsHistory;
import io.coti.common.model.Transactions;
import io.coti.common.services.TransactionHelper;
import io.coti.common.services.interfaces.IClusterService;
import io.coti.common.services.interfaces.IValidationService;
import io.coti.common.services.interfaces.IZeroSpendService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static io.coti.common.http.HttpStringConstants.*;

@Slf4j
@Service
public class TransactionService {
    @Autowired
    private TransactionHelper transactionHelper;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private IClusterService clusterService;
    @Autowired
    private IZeroSpendService zeroSpendService;
    @Autowired
    private ITransactionSender transactionSender;
    @Autowired
    private AddressesTransactionsHistory addressesTransactionsHistory;
    @Autowired
    private Transactions transactions;
    @Autowired
    private ITransactionPropagationSubscriber transactionPropagationReceiver;

    @PostConstruct
    private void init() {
        transactionPropagationReceiver.init(propagatedTransactionConsumer);
    }

    public ResponseEntity<Response> addNewTransaction(AddTransactionRequest request)
            throws TransactionException {
        log.info("New transaction request is being processed. [Transaction Hash={}, Trust score={}", request.hash, request.senderTrustScore);

        if (transactionHelper.isTransactionExists(request.hash)) {
            log.info("Received existing transaction!");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AddTransactionResponse(
                            STATUS_ERROR,
                            TRANSACTION_ALREADY_EXIST_MESSAGE));
        }

        if (!transactionHelper.validateAddresses(
                request.baseTransactions,
                request.hash,
                request.transactionDescription,
                request.senderTrustScore,
                request.createTime)) {
            log.info("Failed to validate addresses!");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AddTransactionResponse(
                            STATUS_ERROR,
                            AUTHENTICATION_FAILED_MESSAGE));
        }

        if (!transactionHelper.isLegalBalance(request.baseTransactions)) {
            log.info("Illegal transaction balance!");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AddTransactionResponse(
                            STATUS_ERROR,
                            ILLEGAL_TRANSACTION_MESSAGE));
        }

        if (!transactionHelper.checkBalancesAndAddToPreBalance(request.baseTransactions)) {
            log.info("Pre balance check failed!");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AddTransactionResponse(
                            STATUS_ERROR,
                            INSUFFICIENT_FUNDS_MESSAGE));
        }
        TransactionData transactionData =
                new TransactionData(
                        request.baseTransactions,
                        request.hash,
                        request.transactionDescription,
                        request.senderTrustScore,
                        request.createTime);
        try {
            transactionData = selectSources(transactionData);

            if (!validationService.validateSource(transactionData.getLeftParentHash()) ||
                    !validationService.validateSource(transactionData.getRightParentHash())) {
                log.info("Could not validate transaction source");
                //TODO: decide what to do here
            }
            transactionData.setPowStartTime(new Date());
            // ############   POW   ###########
            TimeUnit.SECONDS.sleep(5);
            // ################################
            transactionData.setPowEndTime(new Date());

            transactionData.setAttachmentTime(new Date());
            transactionHelper.attachTransactionToCluster(transactionData);
            // TODO: Send to DSP Node
       //     transactionSender.sendTransaction(transactionData);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new AddTransactionResponse(
                            STATUS_SUCCESS,
                            TRANSACTION_CREATED_MESSAGE));
        } catch (Exception ex) {
            log.error("Exception while adding a transaction", ex);
            throw new TransactionException(ex, request.baseTransactions);
        }
    }

    public TransactionData selectSources(TransactionData transactionData) {
        transactionData = clusterService.selectSources(transactionData);
        if (transactionData.hasSources()) {
            return transactionData;
        }

        log.info("No sources found for transaction with trust score {}", transactionData.getSenderTrustScore());
        int retryTimes = 200 / (transactionData.getRoundedSenderTrustScore() + 1);
        while (!transactionData.hasSources() && retryTimes > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();

            }
            retryTimes--;
            transactionData = clusterService.selectSources(transactionData);
            if (transactionData.hasSources()) {
                return transactionData;
            }
        }

        TransactionData zeroSpendTransaction = zeroSpendService.getZeroSpendTransaction(transactionData.getSenderTrustScore());
        transactionHelper.attachTransactionToCluster(zeroSpendTransaction);
        clusterService.attachToCluster(zeroSpendTransaction);
        transactionData = clusterService.selectSources(transactionData);
        while (!transactionData.hasSources()) {
            log.info("Waiting 2 seconds for new zero spend transaction to be added to available sources");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                log.error("Errors when sleeping: {}", e);
            }
            transactionData = clusterService.selectSources(transactionData);
        }
        return transactionData;
    }

    public ResponseEntity<Response> getAddressTransactions(Hash addressHash) {
        List<TransactionData> transactionsDataList = new Vector<>();
        AddressTransactionsHistory history = addressesTransactionsHistory.getByHash(addressHash);
        for (Hash transactionHash : history.getTransactionsHistory()) {
            TransactionData transactionData = transactions.getByHash(transactionHash);
            transactionsDataList.add(transactionData);

        }
        return ResponseEntity.status(HttpStatus.OK).body(new GetAddressTransactionHistory(transactionsDataList));
    }

    private Consumer<TransactionData> propagatedTransactionConsumer = transactionData -> {
        log.info("Transaction received: {}", transactionData.getHash().toHexString());
    };
}
