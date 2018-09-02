package io.coti.fullnode.services;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.data.TransactionStatus;
import io.coti.basenode.http.websocket.GeneratedAddressMessage;
import io.coti.basenode.http.websocket.NotifyTransactionChange;
import io.coti.basenode.http.websocket.UpdatedBalanceMessage;
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
        log.trace("Address {} with balance {} and pre balance {} is about to be sent to the subscribed user", addressHash.toHexString(), balance, preBalance);
        messagingSender.convertAndSend("/topic/" + addressHash.toHexString(),
                new UpdatedBalanceMessage(addressHash, balance, preBalance));
    }

    public void notifyTransactionHistoryChange(TransactionData transactionData, TransactionStatus transactionStatus) {
        log.debug("Transaction {} is about to be sent to the subscribed user", transactionData.getHash().toHexString());

        for (BaseTransactionData bxData : transactionData.getBaseTransactions()) {
            messagingSender.convertAndSend("/topic/addressTransactions/" + bxData.getAddressHash().toHexString(),
                    new NotifyTransactionChange(transactionData, transactionStatus));
        }
    }

    public void notifyGeneratedAddress(Hash addressHash) {
        log.debug("Address {} is about to be sent to the subscribed user", addressHash.toHexString());
        messagingSender.convertAndSend("/topic/address/" + addressHash.toHexString(),
                new GeneratedAddressMessage(addressHash));
    }
}