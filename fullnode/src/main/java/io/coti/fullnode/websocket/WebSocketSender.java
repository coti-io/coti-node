package io.coti.fullnode.websocket;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.data.TransactionStatus;
import io.coti.fullnode.websocket.data.GeneratedAddressMessage;
import io.coti.fullnode.websocket.data.NotifyTransactionChange;
import io.coti.fullnode.websocket.data.TotalTransactionsMessage;
import io.coti.fullnode.websocket.data.UpdatedBalanceMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class WebSocketSender {

    private SimpMessagingTemplate messagingSender;

    @Autowired
    public WebSocketSender(SimpMessagingTemplate simpMessagingTemplate) {
        this.messagingSender = simpMessagingTemplate;
    }

    public void notifyBalanceChange(Hash addressHash, BigDecimal balance, BigDecimal preBalance) {
        log.trace("Address {} with balance {} and pre balance {} is about to be sent to the subscribed user", addressHash, balance, preBalance);
        messagingSender.convertAndSend("/topic/" + addressHash.toString(),
                new UpdatedBalanceMessage(addressHash, balance, preBalance));
    }

    public void notifyTransactionHistoryChange(TransactionData transactionData, TransactionStatus transactionStatus) {
        log.debug("Transaction {} is about to be sent to the subscribed user", transactionData.getHash());
        NotifyTransactionChange notifyTransactionChange = new NotifyTransactionChange(transactionData, transactionStatus);
        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            messagingSender.convertAndSend("/topic/addressTransactions/" + baseTransactionData.getAddressHash().toString(), notifyTransactionChange);
        });
        messagingSender.convertAndSend("/topic/transactions", notifyTransactionChange);
    }

    public void notifyGeneratedAddress(Hash addressHash) {
        log.debug("Address {} is about to be sent to the subscribed user", addressHash);
        messagingSender.convertAndSend("/topic/address/" + addressHash.toString(),
                new GeneratedAddressMessage(addressHash));
    }

    public void notifyTotalTransactionsChange(int totalTransactions) {
        log.debug("Total trasnactions number {} is about to be sent to the subscribed user", totalTransactions);
        messagingSender.convertAndSend("/topic/transaction/total",
                new TotalTransactionsMessage(totalTransactions));
    }
}