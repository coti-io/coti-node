package io.coti.zerospend.services;

import io.coti.common.crypto.TransactionCrypto;
import io.coti.common.data.TransactionData;
import io.coti.common.services.TransactionHelper;
import io.coti.common.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TransactionService {
    @Autowired
    private IValidationService validationService;
    @Autowired
    private TransactionHelper transactionHelper;
    @Autowired
    private TransactionCrypto transactionCrypto;
    @Autowired
    private DspVoteService dspVoteService;

    public void handlePropagatedTransactionFromDspNode(TransactionData transactionData) {

        if (!addTransactionToLocalNode(transactionData)) {
            return;
        }
        dspVoteService.preparePropagatedTransactionForVoting(transactionData);
    }

    private boolean addTransactionToLocalNode(TransactionData transactionData) {
        log.debug("DSP Propagated Transaction received: {}", transactionData.getHash().toHexString());
        if (!transactionHelper.startHandleTransaction(transactionData)) {
            log.debug("Transaction already exists: {}", transactionData.getHash().toHexString());
            return false;
        }
        if (!transactionHelper.validateTransaction(transactionData) ||
                !transactionCrypto.verifySignature(transactionData) ||
                !validationService.validatePow(transactionData)) {
            log.error("Data Integrity validation failed: {}", transactionData.getHash().toHexString());
            return false;
        }
        if (!transactionHelper.checkBalancesAndAddToPreBalance(transactionData)) {
            log.error("Balance check failed: {}", transactionData.getHash().toHexString());
            return false;
        }
        transactionHelper.attachTransactionToCluster(transactionData);
        transactionHelper.setTransactionStateToSaved(transactionData);
        transactionHelper.setTransactionStateToFinished(transactionData);
        transactionHelper.endHandleTransaction(transactionData);
        return true;
    }
}