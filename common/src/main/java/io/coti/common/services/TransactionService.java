package io.coti.common.services;

import io.coti.common.crypto.TransactionCryptoWrapper;
import io.coti.common.data.*;
import io.coti.common.exceptions.TransactionException;
import io.coti.common.http.*;
import io.coti.common.model.AddressesTransactionsHistory;
import io.coti.common.model.Transactions;
import io.coti.common.services.LiveView.WebSocketSender;
import io.coti.common.services.interfaces.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static io.coti.common.http.HttpStringConstants.*;

@Slf4j
@Service
public class TransactionService implements ITransactionService {

    private Map<Hash, TransactionData> propagationTransactionHash;


    @Autowired
    private AddressesTransactionsHistory addressesTransactionsHistory;

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
    @Autowired
    private WebSocketSender webSocketSender;

    @PostConstruct
    private void init() {
        log.info("Transaction io.coti.fullnode.service Started");
        hashToWaitingChildrenTransactionsMapping = new HashMap();
        propagationTransactionHash = new ConcurrentHashMap();
    }

    @Override
    public ResponseEntity<Response> addNewTransaction(AddTransactionRequest request)
            throws TransactionException {

        log.info("New transaction request is being processed. Transaction Hash: {}", request.hash);
        if(isTransactionExists(request.hash)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AddTransactionResponse(
                            STATUS_ERROR,
                            TRANSACTION_ALREADY_EXIST_MESSAGE));
        }

        if (!validateAddresses(request.baseTransactions, request.hash, request.transactionDescription, request.senderTrustScore, request.createTime)) {
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
            TransactionData transactionData = new TransactionData(request.baseTransactions, request.hash, request.transactionDescription,request.senderTrustScore, request.createTime);

            transactionData = selectSources(transactionData);

            if (!validationService.validateSource(transactionData.getLeftParentHash()) ||
                    !validationService.validateSource(transactionData.getRightParentHash())) {
                log.info("Could not validate transaction source");
            }
            transactionData.setPowStartTime(new Date());
            // ############   POW   ###########
            TimeUnit.SECONDS.sleep(5);
            // ################################
            transactionData.setPowEndTime(new Date());

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

    public ResponseEntity<Response> addPropagatedTransaction(AddTransactionRequest request)
            throws TransactionException {
        log.info("Adding a transaction from propagation request is being processed. Transaction Hash: {}", request.hash);

        synchronized (this) {
            if (propagationTransactionHash.containsKey(request.hash) || getTransactionData(request.hash) != null) {
                addNodesToTransaction(request.hash, request.transactionData.getValidByNodes());
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(new AddTransactionResponse(
                                STATUS_ERROR,
                                TRANSACTION_ALREADY_EXIST_MESSAGE));
            }


            propagationTransactionHash.put(request.hash, request.transactionData);
        }

        if (!ifAllTransactionParentsExistInLocalNode(request.transactionData)) {
            propagationTransactionHash.remove(request.hash);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new AddTransactionResponse(
                            STATUS_ERROR,
                            WAITING_FOR_TRANSACTION_PARENT_MESSAGE));
        }

        ResponseEntity<Response> response = addFromPropagationAfterValidation(request.baseTransactions, request.transactionData);
        if ((response.getStatusCode().equals(HttpStatus.CREATED))
                && response.getBody().getStatus().equals(HttpStringConstants.STATUS_SUCCESS)) {
//            propagationService.propagateToNeighbors(request.transactionData);
        }

        propagationTransactionHash.remove(request.hash);
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

        Hash leftParentHash = transactionData.getLeftParentHash();
        Hash rightParentHash = transactionData.getRightParentHash();

        GetTransactionRequest getTransactionRequest = new GetTransactionRequest();
        boolean ifNotNeededToPropagateFromNeighbors =
                ifTransactionParentExistInLocalNode(leftParentHash, getTransactionRequest) &&
                        ifTransactionParentExistInLocalNode(rightParentHash, getTransactionRequest);


        if (!ifNotNeededToPropagateFromNeighbors) {
            hashToWaitingChildrenTransactionsMapping.put(transactionData.getHash(), transactionData);
        }
        return ifNotNeededToPropagateFromNeighbors;
    }

    private boolean ifTransactionParentExistInLocalNode(Hash parentHash, GetTransactionRequest getTransactionRequest) {
        if (parentHash != null && getTransactionData(parentHash) == null) {
            getTransactionRequest.transactionHash = parentHash;
//            propagationService.propagateFromNeighbors(getTransactionRequest.transactionHash);
            return false;
        }
        return true;
    }

    private ResponseEntity<Response> addFromPropagationAfterValidation(List<BaseTransactionData> baseTransactions,
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
            attachTransactionToCluster(transactionData);
            attachWaitingChildren(transactionData);
        } catch (Exception ex) {
            log.error("Error while addFromPropagationAfterValidation", ex);
            throw new TransactionException(ex, baseTransactions);
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

    private boolean validateAddresses(List<BaseTransactionData> baseTransactions, Hash transactionHash, String transactionDescription, Double senderTrustScore, Date createTime) {

        TransactionCryptoWrapper verifyTransaction = new TransactionCryptoWrapper(baseTransactions, transactionHash, transactionDescription, senderTrustScore, createTime);

        return verifyTransaction.isTransactionValid();
    }

    private boolean isTransactionExists(Hash transactionHash){
        TransactionData transaction = getTransactionData(transactionHash);
        return transaction != null;
    }

    @Override
    public TransactionData getTransactionData(Hash transactionHash) {
        return transactions.getByHash(transactionHash);
    }

    @Override
    public ResponseEntity<Response> getTransactionDetails(Hash transactionHash) {
        TransactionData transactionData = transactions.getByHash(transactionHash);
        if(transactionData == null)
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new AddTransactionResponse(
                            STATUS_ERROR,
                            TRANSACTION_DOESNT_EXIST_MESSAGE));
        TransactionResponseData transactionResponseData = new TransactionResponseData();
        transactionResponseData.setHash(transactionData.getHash().toHexString());
        transactionResponseData.setAmount(transactionData.getAmount());
        transactionResponseData.setLeftParentHash( transactionData.getLeftParentHash() == null ? null : transactionData.getLeftParentHash().toHexString());
        transactionResponseData.setRightParentHash(transactionData.getRightParentHash() == null ? null : transactionData.getRightParentHash().toHexString());
        List<String> trustChainTransactionHashes = new Vector<>();
        for(Hash trustChainHash : transactionData.getTrustChainTransactionHashes()){
            trustChainTransactionHashes.add(trustChainHash.toHexString());
        }
        transactionResponseData.setTrustChainTransactionHashes(trustChainTransactionHashes);
        transactionResponseData.setTrustChainConsensus(transactionData.isTrustChainConsensus());
        transactionResponseData.setDspConsensus(transactionData.isDspConsensus());
        transactionResponseData.setTrustChainTrustScore(transactionData.getTrustChainTrustScore());
        transactionResponseData.setTransactionConsensusUpdateTime(transactionData.getTransactionConsensusUpdateTime());
        transactionResponseData.setCreateTime(transactionData.getCreateTime());
        transactionResponseData.setAttachmentTime(transactionData.getAttachmentTime());
        transactionResponseData.setProcessStartTime(transactionData.getProcessStartTime());
        transactionResponseData.setPowStartTime(transactionData.getPowStartTime());
        transactionResponseData.setPowEndTime(transactionData.getPowEndTime());
        transactionResponseData.setSenderTrustScore(transactionData.getSenderTrustScore());

        List<String> childrenTransactions = new Vector<>();
        for(Hash childrenTransaction : transactionData.getChildrenTransactions()){
            childrenTransactions.add(childrenTransaction.toHexString());
        }
        transactionResponseData.setChildrenTransactions(childrenTransactions);
        transactionResponseData.setTransactionDescription(transactionData.getTransactionDescription());
        transactionResponseData.setValid(transactionData.isValid());
        transactionResponseData.setZeroSpend(transactionData.isZeroSpend());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GetTransactionResponse(transactionResponseData));
    }


    @Override
    public ResponseEntity<Response> getAddressTransactions(Hash addressHash) {
        List<TransactionData> transactionsDataList = new ArrayList<>();
        AddressTransactionsHistory history = addressesTransactionsHistory.getByHash(addressHash);
        for (Hash transactionHash: history.getTransactionsHistory()) {
            TransactionData transactionData =  transactions.getByHash(transactionHash);
            transactionsDataList.add(transactionData);

        }
        return ResponseEntity.status(HttpStatus.OK).body(new GetAddressTransactionHistory(transactionsDataList));
    }
}