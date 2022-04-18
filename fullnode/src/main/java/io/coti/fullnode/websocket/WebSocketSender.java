package io.coti.fullnode.websocket;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TokenMintingFeeBaseTransactionData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.data.TokenResponseData;
import io.coti.basenode.http.data.TransactionStatus;
import io.coti.basenode.services.interfaces.ICurrencyService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.basenode.services.interfaces.IWebSocketMessageService;
import io.coti.fullnode.websocket.data.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Component
public class WebSocketSender {

    @Autowired
    private IWebSocketMessageService messagingSender;
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    private ICurrencyService currencyService;

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
        updateMintedAddresses(transactionData, notifyTransactionChange);
        messagingSender.convertAndSend("/topic/transactions", notifyTransactionChange);
        messagingSender.convertAndSend("/topic/transaction/" + transactionData.getHash().toString(), notifyTransactionChange);
    }

    public void notifyTokenChange(Hash currencyHash) {
        log.debug("token {} is about to be sent to the subscribed user", currencyHash.toString());
        TokenResponseData tokenResponseData = currencyService.fillTokenGenerationResponseData(currencyHash);
        TokenChangeMessage tokenChangeMessage = new TokenChangeMessage(tokenResponseData);
        messagingSender.convertAndSend("/topic/token/" + currencyHash.toHexString(), tokenChangeMessage);
    }

    public void updateMintedAddresses(TransactionData transactionData, NotifyTransactionChange notifyTransactionChange) {
        TokenMintingFeeBaseTransactionData tokenMintingFeeBaseTransactionData = transactionHelper.getTokenMintingFeeData(transactionData);
        if (tokenMintingFeeBaseTransactionData != null) {
            Hash receiverAddressHash = tokenMintingFeeBaseTransactionData.getServiceData().getReceiverAddress();
            Optional<BaseTransactionData> identicalAddresses = transactionData.getBaseTransactions().stream().filter(t -> t.getAddressHash().equals(receiverAddressHash)).findFirst();
            if (!identicalAddresses.isPresent()) {
                messagingSender.convertAndSend("/topic/addressTransactions/" + receiverAddressHash.toString(), notifyTransactionChange);
            }
        }
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
