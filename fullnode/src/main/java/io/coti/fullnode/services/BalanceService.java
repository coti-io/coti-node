package io.coti.fullnode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.data.TransactionStatus;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

public class BalanceService extends io.coti.basenode.services.BalanceService {

    @Autowired
    private WebSocketSender webSocketSender;

    @Override
    protected void continueHandleBalanceChanges(Hash addressHash, BigDecimal newBalance, BigDecimal newPreBalance) {
        webSocketSender.notifyBalanceChange(addressHash, newBalance, newPreBalance);
    }

    @Override
    protected void continueHandleAddressHistoryChanges(TransactionData transactionData, TransactionStatus transactionStatus) {
        webSocketSender.notifyTransactionHistoryChange(transactionData, TransactionStatus.CONFIRMED);
    }
}
