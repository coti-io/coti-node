package io.coti.trustscore.services;

import io.coti.common.data.TransactionData;
import io.coti.common.http.data.TransactionStatus;
import io.coti.common.services.BaseNodeTransactionService;
import io.coti.common.services.LiveView.WebSocketSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransactionService extends BaseNodeTransactionService {
    @Autowired
    private WebSocketSender webSocketSender;

    @Override
    protected void continueHandlePropagatedTransaction(TransactionData transactionData) {
        webSocketSender.notifyTransactionHistoryChange(transactionData, TransactionStatus.ATTACHED_TO_DAG);
    }
}
