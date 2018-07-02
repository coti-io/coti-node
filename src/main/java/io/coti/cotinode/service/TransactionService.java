package io.coti.cotinode.service;

import io.coti.cotinode.crypto.CryptoUtils;
import io.coti.cotinode.data.BaseTransactionData;
import io.coti.cotinode.data.ConfirmationData;
import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.http.AddTransactionRequest;
import io.coti.cotinode.http.AddTransactionResponse;
import io.coti.cotinode.http.GetTransactionResponse;
import io.coti.cotinode.model.Transactions;
import io.coti.cotinode.service.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.coti.cotinode.http.HttpStringConstants.*;

@Slf4j
@Service
public class TransactionService implements ITransactionService {

    @Autowired
    private IZeroSpendService zeroSpendService;
    @Autowired
    private IBalanceService balanceService;
    @Autowired
    private IClusterService clusterService;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private Transactions transactions;
    @Autowired
    private PropagationService propagationService;

    private HashMap<Hash, TransactionData> hashToWaitingChildrenTransactionsMapping;

    @PostConstruct
    private void init() {
        log.info("Transaction service Started");
        hashToWaitingChildrenTransactionsMapping = new HashMap();
    }

    @Override
    public ResponseEntity<AddTransactionResponse> addNewTransaction(AddTransactionRequest request) {
        log.info("New transaction request is being processed. Transaction Hash: {}", request.transactionHash);
        if (!validateAddresses(request)) {
            log.info("Failed to validate addresses!");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AddTransactionResponse(
                            STATUS_ERROR,
                            AUTHENTICATION_FAILED_MESSAGE));
        }

        if (!isLegalBalance(request.baseTransactions)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AddTransactionResponse(
                            STATUS_ERROR,
                            ILLEGAL_TRANSACTION_MESSAGE));
        }

        if (!balanceService.checkBalancesAndAddToPreBalance(request.baseTransactions)) {
            log.info("Pre balance check failed!");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AddTransactionResponse(
                            STATUS_ERROR,
                            INSUFFICIENT_FUNDS_MESSAGE));
        }

        TransactionData transactionData = new TransactionData(request);

        transactionData = selectSources(transactionData);

        if (!validationService.validateSource(transactionData.getLeftParentHash()) ||
                !validationService.validateSource(transactionData.getRightParentHash())) {
            log.info("Could not validate transaction source");
        }

        //POW:
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        attachTransactionToCluster(transactionData);

        propagationService.propagateToNeighbors(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AddTransactionResponse(
                        STATUS_SUCCESS,
                        TRANSACTION_CREATED_MESSAGE));
    }

    public ResponseEntity<AddTransactionResponse> addTransactionFromPropagation(AddTransactionRequest request) {
        log.info("Adding a transaction from propagation request is being processed. Transaction Hash: {}", request.transactionHash);

        if (getTransactionData(request.transactionHash) != null) {
            log.info("Transaction already exist in local node!");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AddTransactionResponse(
                            STATUS_ERROR,
                            TRANSACTION_ALREADY_EXIST_MESSAGE));
        }

        propagationService.propagateToNeighbors(request);

        if (!ifAllTransactionParentsExistInLocalNode(request.transactionData)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AddTransactionResponse(
                            STATUS_ERROR,
                            WAITING_FOR_TRANSACTION_PARENT_MESSAGE));
        }

        ResponseEntity<AddTransactionResponse> transactionStatusAfterValidation = addFromPropagationAfterValidation(request.baseTransactions, request.transactionData);
        // TODO: if transaction is "ok" or "not ok" need to spread answer accordingly

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AddTransactionResponse(
                        STATUS_ERROR,
                        TRANSACTION_FROM_PROPAGATION_MESSAGE));


    }

    public void attachWaitingChildren(TransactionData parentTransactionData) {

        // Loop on every child of the transaction
        for (Hash childHash : parentTransactionData.getChildrenTransactions()) {
            TransactionData childTransactionData = hashToWaitingChildrenTransactionsMapping.get(childHash);
            if (childTransactionData != null) {
                Hash leftParentHash = childTransactionData.getLeftParentHash();
                Hash rightParentHash = childTransactionData.getRightParentHash();

                // If all the child parents are in the local node, add the waiting child to the cluster.
                if ((leftParentHash == null || getTransactionData(leftParentHash) != null)
                        && (rightParentHash == null || getTransactionData(rightParentHash) != null)) {
                    addFromPropagationAfterValidation(childTransactionData.getBaseTransactions(), childTransactionData);
                    hashToWaitingChildrenTransactionsMapping.remove(childHash);
                }
            }
        }
    }

    private boolean ifAllTransactionParentsExistInLocalNode(TransactionData transactionData) {
        boolean ifAllParentsExistInLocalNode = true;
        Hash leftParentHash = transactionData.getLeftParentHash();
        Hash rightParentHash = transactionData.getRightParentHash();

        if (leftParentHash != null && getTransactionData(transactionData.getLeftParentHash()) == null) {
            ifAllParentsExistInLocalNode = false;
            propagationService.getTransactionFromNeighbors(leftParentHash);
        }

        if (rightParentHash != null && getTransactionData(transactionData.getRightParentHash()) == null) {
            ifAllParentsExistInLocalNode = false;
            propagationService.getTransactionFromNeighbors(rightParentHash);
        }
        if (!ifAllParentsExistInLocalNode) {
            hashToWaitingChildrenTransactionsMapping.put(transactionData.getHash(), transactionData);
        }
        return ifAllParentsExistInLocalNode;
    }


    private ResponseEntity<AddTransactionResponse> addFromPropagationAfterValidation(List<BaseTransactionData> baseTransactions,
                                                                                    TransactionData transactionData) {
        if (!isLegalBalance(baseTransactions)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AddTransactionResponse(
                            STATUS_ERROR,
                            ILLEGAL_TRANSACTION_MESSAGE));
        }

        if (!balanceService.checkBalancesAndAddToPreBalance(baseTransactions)) {
            log.info("Pre balance check failed!");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AddTransactionResponse(
                            STATUS_ERROR,
                            INSUFFICIENT_FUNDS_MESSAGE));
        }


        // TODO: Validate POW:

        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        attachTransactionToCluster(transactionData);

        attachWaitingChildren(transactionData);
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new AddTransactionResponse(
                        STATUS_ERROR,
                        AUTHENTICATION_FAILED_MESSAGE));
    }
    private boolean isLegalBalance(List<BaseTransactionData> baseTransactions) {
        BigDecimal totalTransactionSum = BigDecimal.ZERO;
        for (BaseTransactionData baseTransactionData :
                baseTransactions) {
            totalTransactionSum = totalTransactionSum.add(baseTransactionData.getAmount());
        }
        return totalTransactionSum.compareTo(BigDecimal.ZERO) == 0;
    }

    private TransactionData selectSources(TransactionData transactionData) {
        transactionData = clusterService.selectSources(transactionData);
        if (transactionData.hasSources()) {
            return transactionData;
        }

        log.info("No sources found for transaction with trust score {}", transactionData.getSenderTrustScore());
        int retryTimes = 200 / transactionData.getRoundedSenderTrustScore();
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
        attachTransactionToCluster(zeroSpendTransaction);
        clusterService.attachToCluster(zeroSpendTransaction);
        transactionData = clusterService.selectSources(transactionData);
        while (!transactionData.hasSources()) {
            log.info("Waiting 2 seconds for new zero spend transaction to be added to available sources");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            transactionData = clusterService.selectSources(transactionData);
        }
        return transactionData;
    }

    private void attachTransactionToCluster(TransactionData transactionData) {
        transactionData.setAttachmentTime(new Date());
        transactions.put(transactionData);
        if (balanceService.insertToUnconfirmedTransactions(new ConfirmationData(transactionData))) {
            clusterService.attachToCluster(transactionData);
        }
    }

    private boolean validateAddresses(AddTransactionRequest request) {
        for (BaseTransactionData baseTransactionData : request.baseTransactions) {

            if (baseTransactionData.getAmount().signum() > 0) {
                return true;
            }

            if (!validationService.validateSenderAddress(
                    request.message,
                    CryptoUtils.convertSignatureFromString(baseTransactionData.getSignature()),
                    baseTransactionData.getAddressHash())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public TransactionData getTransactionData(Hash transactionHash) {
        return transactions.getByHash(transactionHash);
    }

    @Override
    public ResponseEntity<GetTransactionResponse> getTransactionDetails(Hash transactionHash) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GetTransactionResponse(transactions.getByHash(transactionHash)));
    }
}