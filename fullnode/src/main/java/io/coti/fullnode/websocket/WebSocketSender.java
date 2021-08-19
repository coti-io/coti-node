package io.coti.fullnode.websocket;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.data.TransactionStatus;
import io.coti.basenode.services.interfaces.IWebSocketMessageService;
import io.coti.fullnode.websocket.data.GeneratedAddressMessage;
import io.coti.fullnode.websocket.data.NotifyTransactionChange;
import io.coti.fullnode.websocket.data.TotalTransactionsMessage;
import io.coti.fullnode.websocket.data.UpdatedBalanceMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class WebSocketSender {

    @Autowired
    private IWebSocketMessageService messagingSender;

    public void notifyBalanceChange(Hash addressHash, Hash currencyHash, BigDecimal balance, BigDecimal preBalance) {
        log.trace("Address {} with currency {} , balance {} and pre balance {} is about to be sent to the subscribed user", addressHash, currencyHash, balance, preBalance);
        messagingSender.convertAndSend("/topic/" + addressHash.toString(),
                new UpdatedBalanceMessage(addressHash, currencyHash, balance, preBalance));
    }

    public void notifyTransactionHistoryChange(TransactionData transactionData, TransactionStatus transactionStatus) {
        log.debug("Transaction {} is about to be sent to the subscribed user", transactionData.getHash());
        NotifyTransactionChange notifyTransactionChange = new NotifyTransactionChange(transactionData, transactionStatus);
        transactionData.getBaseTransactions().forEach(baseTransactionData ->
                messagingSender.convertAndSend("/topic/addressTransactions/" + baseTransactionData.getAddressHash().toString(), notifyTransactionChange)
        );
        messagingSender.convertAndSend("/topic/transactions", notifyTransactionChange);
        messagingSender.convertAndSend("/topic/transaction/" + transactionData.getHash().toString(), notifyTransactionChange);
    }

    public void notifyGeneratedAddress(Hash addressHash) {
        log.debug("Address {} is about to be sent to the subscribed user", addressHash);
        messagingSender.convertAndSend("/topic/address/" + addressHash.toString(),
                new GeneratedAddressMessage(addressHash));
    }

    public void notifyTotalTransactionsChange(int totalTransactions) {
        log.debug("Total transactions number {} is about to be sent to the subscribed user", totalTransactions);
        messagingSender.convertAndSend("/topic/transaction/total",
                new TotalTransactionsMessage(totalTransactions));
    }
}