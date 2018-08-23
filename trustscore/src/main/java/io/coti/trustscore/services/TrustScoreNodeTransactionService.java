package io.coti.trustscore.services;

import io.coti.common.data.DspConsensusResult;
import io.coti.common.data.TransactionData;
import io.coti.common.http.data.TransactionStatus;
import io.coti.common.services.TransactionService;
import io.coti.common.services.LiveView.WebSocketSender;
import io.coti.common.services.interfaces.IBalanceService;
import io.coti.common.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TrustScoreNodeTransactionService extends TransactionService {
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private IBalanceService balanceService;
    @Autowired
    private WebSocketSender webSocketSender;

    @Override
    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
        webSocketSender.notifyTransactionHistoryChange(transactionData, TransactionStatus.ATTACHED_TO_DAG);
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
