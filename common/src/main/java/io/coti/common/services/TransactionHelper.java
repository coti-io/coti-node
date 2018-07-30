package io.coti.common.services;

import io.coti.common.crypto.TransactionCryptoWrapper;
import io.coti.common.data.*;
import io.coti.common.exceptions.TransactionException;
import io.coti.common.http.*;
import io.coti.common.http.data.TransactionResponseData;
import io.coti.common.model.AddressesTransactionsHistory;
import io.coti.common.model.Transactions;
import io.coti.common.services.interfaces.IBalanceService;
import io.coti.common.services.interfaces.IClusterService;
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
public class TransactionHelper {

    @Autowired
    private AddressesTransactionsHistory addressesTransactionsHistory;

    private HashMap<Hash, TransactionData> hashToWaitingChildrenTransactionsMapping;

    @Autowired
    private IBalanceService balanceService;
    @Autowired
    private IClusterService clusterService;
    @Autowired
    private Transactions transactions;

    @PostConstruct
    private void init() {
        log.info("Transaction Helper Started");
        hashToWaitingChildrenTransactionsMapping = new HashMap();
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
            transactionData.setAttachmentTime(new Date());
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

    public boolean isLegalBalance(List<BaseTransactionData> baseTransactions) {
        BigDecimal totalTransactionSum = BigDecimal.ZERO;
        for (BaseTransactionData baseTransactionData :
                baseTransactions) {
            totalTransactionSum = totalTransactionSum.add(baseTransactionData.getAmount());
        }
        return totalTransactionSum.compareTo(BigDecimal.ZERO) == 0;
    }

    private void updateAddressTransactionHistory(TransactionData transactionData) {
        for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
            AddressTransactionsHistory addressHistory = addressesTransactionsHistory.getByHash(baseTransactionData.getAddressHash());

            if (addressHistory == null) {
                addressHistory = new AddressTransactionsHistory(baseTransactionData.getAddressHash());
            }
            addressHistory.addTransactionHashToHistory(transactionData.getHash());
            addressesTransactionsHistory.put(addressHistory);
        }
    }

    public boolean validateTransaction(TransactionData transactionData) {
        TransactionCryptoWrapper verifyTransaction = new TransactionCryptoWrapper(transactionData);
        return verifyTransaction.isTransactionValid();
    }

    public boolean isTransactionExists(Hash transactionHash) {
        TransactionData transaction = getTransactionData(transactionHash);
        return transaction != null;
    }

    public boolean validateDataIntegrity(TransactionData transactionData) {
//        return validateAddresses(transactionData.getBaseTransactions(), transactionData.getHash(), transactionData.getTransactionDescription(), transactionData.getSenderTrustScore(), transactionData.getCreateTime());
        return true;
    }

    public TransactionData getTransactionData(Hash transactionHash) {
        return transactions.getByHash(transactionHash);
    }

    public ResponseEntity<BaseResponse> getTransactionDetails(Hash transactionHash) {
        TransactionData transactionData = transactions.getByHash(transactionHash);
        if (transactionData == null)
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new AddTransactionResponse(
                            STATUS_ERROR,
                            TRANSACTION_DOESNT_EXIST_MESSAGE));
        TransactionResponseData transactionResponseData = new TransactionResponseData(transactionData);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GetTransactionResponse(transactionResponseData));
    }

    public boolean checkBalancesAndAddToPreBalance(List<BaseTransactionData> baseTransactions) {
        return balanceService.checkBalancesAndAddToPreBalance(baseTransactions);
    }

    public void attachTransactionToCluster(TransactionData transactionData) {
        transactions.put(transactionData);
        updateAddressTransactionHistory(transactionData);
        balanceService.insertToUnconfirmedTransactions(new ConfirmationData(transactionData));
        clusterService.attachToCluster(transactionData);
    }
}