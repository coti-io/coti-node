package io.coti.fullnode.services;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.data.TransactionStatus;
import io.coti.basenode.services.BaseNodeConfirmationService;
import io.coti.fullnode.websocket.WebSocketSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConfirmationService extends BaseNodeConfirmationService {

    @Autowired
    private WebSocketSender webSocketSender;

    @Override
    protected void continueHandleAddressHistoryChanges(TransactionData transactionData) {
        webSocketSender.notifyTransactionHistoryChange(transactionData, TransactionStatus.CONFIRMED);
    }
}
