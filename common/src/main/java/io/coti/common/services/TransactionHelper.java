package io.coti.common.services;

import io.coti.common.crypto.TransactionCryptoWrapper;
import io.coti.common.data.*;
import io.coti.common.exceptions.TransactionException;
import io.coti.common.http.AddTransactionResponse;
import io.coti.common.http.BaseResponse;
import io.coti.common.http.GetTransactionResponse;
import io.coti.common.http.data.TransactionResponseData;
import io.coti.common.model.AddressesTransactionsHistory;
import io.coti.common.model.Transactions;
import io.coti.common.services.interfaces.IBalanceService;
import io.coti.common.services.interfaces.IClusterService;
import io.coti.common.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.coti.common.data.TransactionState.*;
import static io.coti.common.http.HttpStringConstants.STATUS_ERROR;
import static io.coti.common.http.HttpStringConstants.TRANSACTION_DOESNT_EXIST_MESSAGE;

@Slf4j
@Service
public class TransactionHelper implements ITransactionHelper {

    @Autowired
    private AddressesTransactionsHistory addressesTransactionsHistory;
    @Autowired
    private IBalanceService balanceService;
    @Autowired
    private IClusterService clusterService;
    @Autowired
    private Transactions transactions;
    private Map<Hash, TransactionState> hashToTransactionStateMapping;

    @PostConstruct
    private void init() {
        hashToTransactionStateMapping = new ConcurrentHashMap<>();
        log.info("Transaction Helper Started");
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

    private boolean isTransactionExists(TransactionData transactionData) {
        if (hashToTransactionStateMapping.containsKey(transactionData.getHash())) {
            return true;
        }
        if (transactions.getByHash(transactionData.getHash()) != null) {
            return true;
        }
        return false;
    }

    public boolean startHandleTransaction(TransactionData transactionData) {
        synchronized (transactionData) {
            if (isTransactionExists(transactionData)) {
                return false;
            }
            hashToTransactionStateMapping.put(transactionData.getHash(), RECEIVED);
            return true;
        }
    }

    public void endHandleTransaction(TransactionData transactionData) {
        if (hashToTransactionStateMapping.get(transactionData.getHash()) == FINISHED) {
            log.info("Transaction handled successfully...");
        } else {
            rollbackTransaction(transactionData);
        }

        synchronized (transactionData) {
            hashToTransactionStateMapping.remove(transactionData.getHash());
        }
    }

    private void rollbackTransaction(TransactionData transactionData) {
        if (hashToTransactionStateMapping.get(transactionData.getHash()) == SAVED_IN_DB) {
            log.error("Reverting transaction saved in DB");
            hashToTransactionStateMapping.replace(transactionData.getHash(), PRE_BALANCE_CHANGED);
        }
        if (hashToTransactionStateMapping.get(transactionData.getHash()) == PRE_BALANCE_CHANGED) {
            log.error("Reverting pre balance...");
            balanceService.rollbackBaseTransactions(transactionData);
            hashToTransactionStateMapping.replace(transactionData.getHash(), RECEIVED);
        }
        if (hashToTransactionStateMapping.get(transactionData.getHash()) == RECEIVED) {
            log.error("Reverting received transaction...");
        }
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

    public boolean checkBalancesAndAddToPreBalance(TransactionData transactionData) {
        if (!hashToTransactionStateMapping.containsKey(transactionData.getHash())) {
            return false;
        }
        if (!(hashToTransactionStateMapping.get(transactionData.getHash()) == RECEIVED)) {
            return false;
        }
        if (!balanceService.checkBalancesAndAddToPreBalance(transactionData.getBaseTransactions())) {
            return false;
        }
        return hashToTransactionStateMapping.replace(transactionData.getHash(), RECEIVED, PRE_BALANCE_CHANGED);
    }

    public void attachTransactionToCluster(TransactionData transactionData) {
        transactions.put(transactionData);
        updateAddressTransactionHistory(transactionData);
        balanceService.insertToUnconfirmedTransactions(new ConfirmationData(transactionData));
        clusterService.attachToCluster(transactionData);
    }

    public void setTransactionStateToSaved(TransactionData transactionData) {
        if (!(hashToTransactionStateMapping.get(transactionData.getHash()) == PRE_BALANCE_CHANGED)) {
            throw new TransactionException("Transaction to be saved is not in Pre balance changed stage!");
        } else {
            hashToTransactionStateMapping.replace(transactionData.getHash(), SAVED_IN_DB);
        }
    }

    public void setTransactionStateToFinished(TransactionData transactionData) {
        if (!(hashToTransactionStateMapping.get(transactionData.getHash()) == SAVED_IN_DB)) {
            throw new TransactionException("Transaction to be propagated is not saved in DB!");
        } else {
            hashToTransactionStateMapping.replace(transactionData.getHash(), FINISHED);
        }
    }

}