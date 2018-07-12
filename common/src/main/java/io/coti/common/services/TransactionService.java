package io.coti.common.services;

import io.coti.common.crypto.TransactionCryptoDecorator;
import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.ConfirmationData;
import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.exceptions.TransactionException;
import io.coti.common.http.*;
import io.coti.common.model.Transactions;
import io.coti.common.services.LiveView.WebSocketSender;
import io.coti.common.services.interfaces.*;
import io.coti.fullnode.service.PropagationService;
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

import static io.coti.common.http.HttpStringConstants.*;

@Slf4j
@Service
public class TransactionService implements ITransactionService {
    private Map<Hash, TransactionData> hashToWaitingChildrenTransactionsMapping;
    private Map<Hash, TransactionData> propagationTransactionHash;


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
    @Autowired
    private WebSocketSender webSocketSender;

    @PostConstruct
    private void init() {
        log.info("Transaction io.coti.fullnode.service Started");
        hashToWaitingChildrenTransactionsMapping = new HashMap();
        propagationTransactionHash = new ConcurrentHashMap();
        //propagateMultiTransactionFromDsp();
    }

    @Override
    public void propagateMultiTransactionFromDsp() {

        List<TransactionData> transactionsFromDsp = propagationService.propagateMultiTransactionFromDsp(propagationService.getLastIndex());
        if (transactionsFromDsp != null) {
            transactionsFromDsp.forEach(transaction -> {
                if (transaction.getIndex() > 0) {
                    setTransactionConfirmedFromPropagation(transaction);
                }
                else {
                    addTransactionFromPropagation(transaction);
                }
            });
        }
    }

    @Override
    public ResponseEntity<Response> addNewTransaction(AddTransactionRequest request)
            throws TransactionException {

        log.info("New transaction request is being processed. Transaction Hash: {}", request.hash);
        if (!validateAddresses(request.baseTransactions, request.hash, request.transactionDescription, request.senderTrustScore)) {
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
            TransactionData transactionData = new TransactionData(request.baseTransactions, request.hash, request.transactionDescription,request.senderTrustScore);

            transactionData = selectSources(transactionData);

            if (!validationService.validateSource(transactionData.getLeftParentHash()) ||
                    !validationService.validateSource(transactionData.getRightParentHash())) {
                log.info("Could not validate transaction source");
            }

            // ############   POW   ###########
            TimeUnit.SECONDS.sleep(5);
            // ################################
            attachTransactionToCluster(transactionData);
//            transactionData.setSenderNodeIpAddress(propagationService.getCurrentNodeIp());

//            propagationService.propagateToNeighbors(transactionData);
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

    @Override
    public TransactionData getLastTransactionHash() {
        return transactions.getByHash(propagationService.getLastTransactionHash());
    }

    @Override
    public void addTransactionFromPropagation(TransactionData transactionData) throws TransactionException {
        log.info("Adding a transaction from propagation. Transaction Hash: {}", transactionData.getHash());
        Hash transactionHash = transactionData.getHash();
        synchronized (this) {
            if (propagationTransactionHash.containsKey(transactionHash) || transactions.getByHash(transactionHash) != null) {
                return;
            }
            propagationTransactionHash.put(transactionHash, transactionData);
        }

        if (!ifAllTransactionParentsExistInLocalNode(transactionData) ) {
            addFromPropagationAfterValidation(transactionData);
        }
    }

    @Override
    public void setTransactionConfirmedFromPropagation(TransactionData transactionData) throws TransactionException {
        // TODO: Validate signatures
        transactions.put(transactionData);
        propagationService.updateLastIndex(transactionData);
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
                if ((leftParentHash == null || getTransactionData(leftParentHash) != null) && (rightParentHash == null || getTransactionData(rightParentHash) != null)) {
                    addFromPropagationAfterValidation(childTransactionData);
                    hashToWaitingChildrenTransactionsMapping.remove(childHash);
                }
            }
        }
    }

    @Override
    public TransactionData getTransactionData(Hash transactionHash) {
        return transactions.getByHash(transactionHash);
    }

    @Override
    public ResponseEntity<Response> getTransactionDetails(Hash transactionHash) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GetTransactionResponse(transactions.getByHash(transactionHash)));
    }

    private boolean ifAllTransactionParentsExistInLocalNode(TransactionData transactionData) {

        Hash leftParentHash = transactionData.getLeftParentHash();
        Hash rightParentHash = transactionData.getRightParentHash();

        boolean transactionParentExistInLocalNode =
                ifTransactionParentExistInLocalNode(leftParentHash)
                        && ifTransactionParentExistInLocalNode(rightParentHash);

        if (!transactionParentExistInLocalNode) {
            hashToWaitingChildrenTransactionsMapping.put(transactionData.getHash(), transactionData);
        }
        return transactionParentExistInLocalNode;
    }

    private boolean ifTransactionParentExistInLocalNode(Hash parentHash ) {
        if (parentHash != null && getTransactionData(parentHash) == null) {
            propagationService.propagateTransactionFromDspByHash(parentHash);
            return false;
        }
        return true;
    }

    private ResponseEntity<Response> addFromPropagationAfterValidation(TransactionData transactionData) {
        if (!isLegalBalance(transactionData.getBaseTransactions())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AddTransactionResponse(STATUS_ERROR, ILLEGAL_TRANSACTION_MESSAGE));
        }

        if (!balanceService.checkBalancesAndAddToPreBalance(transactionData.getBaseTransactions())) {
            log.info("Pre balance check failed!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AddTransactionResponse(STATUS_ERROR, INSUFFICIENT_FUNDS_MESSAGE));
        }


        // TODO: Validate POW:

        try {
            TimeUnit.SECONDS.sleep(5);
            attachTransactionToCluster(transactionData);
            attachWaitingChildren(transactionData);
        } catch (Exception ex) {
            log.error("Error while addFromPropagationAfterValidation", ex);
            throw new TransactionException(ex, transactionData.getBaseTransactions());

        }

        return ResponseEntity.status(HttpStatus.CREATED).body(new AddTransactionResponse(STATUS_SUCCESS, TRANSACTION_CREATED_MESSAGE));
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
        attachTransactionToCluster(zeroSpendTransaction);
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

    private void attachTransactionToCluster(TransactionData transactionData) {
        transactionData.setAttachmentTime(new Date());
        transactions.put(transactionData);
        balanceService.insertToUnconfirmedTransactions(new ConfirmationData(transactionData));
        clusterService.attachToCluster(transactionData);

    }

    private boolean validateAddresses(List<BaseTransactionData> baseTransactions, Hash transactionHash, String transactionDescription, Double senderTrustScore) {

        TransactionCryptoDecorator verifyTransaction = new TransactionCryptoDecorator(baseTransactions, transactionHash, transactionDescription, senderTrustScore);
        for (BaseTransactionData baseTransactionData : baseTransactions) {

            if (baseTransactionData.getAmount().signum() > 0) {
                return true;
            }
        }
        return verifyTransaction.isTransactionValid();
    }


}