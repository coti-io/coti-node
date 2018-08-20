package io.coti.trustscore.services;

import io.coti.common.crypto.TransactionCrypto;
import io.coti.common.data.DspConsensusResult;
import io.coti.common.data.TransactionData;
import io.coti.common.http.data.TransactionStatus;
import io.coti.common.services.LiveView.WebSocketSender;
import io.coti.common.services.interfaces.IBalanceService;
import io.coti.common.services.interfaces.ITransactionHelper;
import io.coti.common.services.interfaces.IValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransactionService {
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private TransactionCrypto transactionCrypto;
    @Autowired
    private IValidationService validationService;
    @Autowired
    private IBalanceService balanceService;
    @Autowired
    private WebSocketSender webSocketSender;


    public void handlePropagatedTransaction(TransactionData transactionData) {
        log.debug("Propagated Transaction received: {}", transactionData.getHash().toHexString());
        if (!transactionHelper.startHandleTransaction(transactionData)) {
            log.debug("Transaction already exists: {}", transactionData.getHash().toHexString());
            return;
        }
        if (!transactionHelper.validateTransaction(transactionData) ||
                !transactionCrypto.verifySignature(transactionData) ||
                !validationService.validatePow(transactionData)) {
            log.error("Data Integrity validation failed: {}", transactionData.getHash().toHexString());
            return;
        }
        if (!transactionHelper.checkBalancesAndAddToPreBalance(transactionData)) {
            log.error("Balance check failed: {}", transactionData.getHash().toHexString());
            return;
        }
        transactionHelper.attachTransactionToCluster(transactionData);
        transactionHelper.setTransactionStateToSaved(transactionData);
        webSocketSender.notifyTransactionHistoryChange(transactionData, TransactionStatus.ATTACHED_TO_DAG);
        transactionHelper.setTransactionStateToFinished(transactionData);
        transactionHelper.endHandleTransaction(transactionData);
    }

    public void handleVoteConclusion(DspConsensusResult dspConsensusResult) {
        log.debug("Received DspConsensus result for transaction: {}", dspConsensusResult.getHash());
        if (!transactionHelper.handleVoteConclusionResult(dspConsensusResult)) {
            log.error("Illegal Dsp consensus result for transaction: {}", dspConsensusResult.getHash());
        } else {
            balanceService.setDspcToTrue(dspConsensusResult);
        }
    }
}
