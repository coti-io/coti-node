package io.coti.cotinode.service;

import io.coti.cotinode.crypto.CryptoUtils;
import io.coti.cotinode.data.BaseTransactionData;
import io.coti.cotinode.data.ConfirmationData;
import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.exception.TransactionException;
import io.coti.cotinode.http.AddTransactionRequest;
import io.coti.cotinode.http.AddTransactionResponse;
import io.coti.cotinode.http.GetTransactionResponse;
import io.coti.cotinode.http.HttpStringConstants;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static io.coti.cotinode.http.HttpStringConstants.*;

@Slf4j
@Service
public class TransactionService implements ITransactionService {

    private Map<Hash, TransactionData> propagationTransactionHash;

    private HashMap<Hash, TransactionData> hashToWaitingChildrenTransactionsMapping;

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

    @PostConstruct
    private void init() {
        log.info("Transaction service Started");
        hashToWaitingChildrenTransactionsMapping = new HashMap();
        propagationTransactionHash = new ConcurrentHashMap();
    }

    @Override
    public ResponseEntity<AddTransactionResponse> addNewTransaction(AddTransactionRequest request)
            throws TransactionException {
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
        try {

            TransactionData transactionData = new TransactionData(request);

            transactionData = selectSources(transactionData);

            if (!validationService.validateSource(transactionData.getLeftParentHash()) ||
                    !validationService.validateSource(transactionData.getRightParentHash())) {
                log.info("Could not validate transaction source");
            }

            // ############   POW   ###########
            TimeUnit.SECONDS.sleep(5);
            // ################################
            attachTransactionToCluster(transactionData);
            transactionData.setSenderNodeIpAddress(propagationService.getCurrentNodeIp());
            transactionData.setValid(true);// It's needed?
            propagationService.propagateToNeighbors(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new AddTransactionResponse(
                            STATUS_SUCCESS,
                            TRANSACTION_CREATED_MESSAGE));
        } catch (Exception ex) {
            log.error("Exception while adding a transaction", ex);
            throw new TransactionException(ex);

        }
    }

    public ResponseEntity<AddTransactionResponse> addTransactionFromPropagation(AddTransactionRequest request)
            throws TransactionException {
        log.info("Adding a transaction from propagation request is being processed. Transaction Hash: {}", request.transactionHash);

        synchronized (this) {
            if (propagationTransactionHash.containsKey(request.transactionHash) || getTransactionData(request.transactionHash) != null) {
                addNodesToTransaction(request.transactionHash, request.transactionData.getValidByNodes());
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new AddTransactionResponse(
                                STATUS_ERROR,
                                TRANSACTION_ALREADY_EXIST_MESSAGE));
            }
            propagationTransactionHash.put(request.transactionHash, request.transactionData);
        }

        if (!ifAllTransactionParentsExistInLocalNode(request.transactionData)) {
            propagationTransactionHash.remove(request.transactionHash);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AddTransactionResponse(
                            STATUS_ERROR,
                            WAITING_FOR_TRANSACTION_PARENT_MESSAGE));
        }
        ResponseEntity<AddTransactionResponse> response = addFromPropagationAfterValidation(request.baseTransactions, request.transactionData);
        if ((response.getStatusCode().equals(HttpStatus.CREATED))
                && response.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS)) {
            propagationService.propagateToNeighbors(request);
        }

        propagationTransactionHash.remove(request.transactionHash);
        return response;
    }



    private void addNodesToTransaction(Hash transactionHash, Map<String, Boolean> validByNodes) {
        if (propagationTransactionHash.containsKey(transactionHash)) {
            propagationTransactionHash.get(transactionHash).addNodesToTransaction(validByNodes);
        }
        TransactionData transaction = getTransactionData(transactionHash);
        if (transaction != null) {
            transaction.addNodesToTransaction(validByNodes);
            transactions.put(transaction);
        }
    }

    public void attachWaitingChildren(TransactionData parentTransactionData) throws TransactionException {

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


    private ResponseEntity<AddTransactionResponse> addFromPropagationAfterValidation
            (List<BaseTransactionData> baseTransactions, TransactionData transactionData) throws TransactionException {
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

            attachTransactionToCluster(transactionData);

            attachWaitingChildren(transactionData);
        } catch (Exception ex) {
            log.error("Error while addFromPropagationAfterValidation", ex);
            throw new TransactionException(ex);

        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AddTransactionResponse(
                        STATUS_SUCCESS,
                        TRANSACTION_CREATED_MESSAGE));
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
                TimeUnit.SECONDS.sleep(2);
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