package io.coti.basenode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.services.interfaces.ITransactionHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static io.coti.basenode.services.BaseNodeServiceManager.addressTransactionsHistories;
import static io.coti.basenode.services.BaseNodeServiceManager.nodeTransactionHelper;

@Service
@Slf4j
public class BaseNodeTransactionHistoryService implements ITransactionHistoryService {

    private ExecutorService transactionHistoryExecutorService;
    private ConcurrentHashMap<Integer, Future<?>> historyFutures;

    public void init() {
        transactionHistoryExecutorService = Executors.newFixedThreadPool(10);
        historyFutures = new ConcurrentHashMap<>();
        log.info("{} is up", this.getClass().getSimpleName());
    }

    public void addToHistory(TransactionData transactionData) {
        Future<?> historyFuture = transactionHistoryExecutorService.submit(() ->
        {
            transactionData.getBaseTransactions().forEach(baseTransactionData ->
                    updateAddressTransactionsHistories(baseTransactionData.getAddressHash(), transactionData)
            );
            updateMintedAddress(transactionData);
        });
        historyFutures.put(historyFuture.hashCode(), historyFuture);

        transactionHistoryExecutorService.submit(() -> monitorFuture(historyFuture, transactionData.getHash()));
    }

    private void monitorFuture(Future<?> historyFuture, Hash transactionHash) {

        while (!historyFuture.isDone() && !historyFuture.isCancelled()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (historyFuture.isCancelled()) {
            log.error("History future cancelled for transaction: {}", transactionHash);
        } else {
            log.debug("History future done for transaction: {}", transactionHash);
        }
    }

    public int getNumberOfTransactionsWaitingHistoryUpdates() {
        return historyFutures.size();
    }

    private void updateAddressTransactionsHistories(Hash addressHash, TransactionData transactionData) {
        AddressTransactionsHistory addressHistory = Optional.ofNullable(addressTransactionsHistories.getByHash(addressHash))
                .orElse(new AddressTransactionsHistory(addressHash));

        if (!addressHistory.addTransactionHashToHistory(transactionData.getHash())) {
            log.debug("Transaction {} is already in history of address {}", transactionData.getHash(), addressHash);
        }
        addressTransactionsHistories.put(addressHistory);
    }

    public void updateMintedAddress(TransactionData transactionData) {
        TokenMintingFeeBaseTransactionData tokenMintingFeeBaseTransactionData = nodeTransactionHelper.getTokenMintingFeeData(transactionData);
        if (tokenMintingFeeBaseTransactionData != null) {
            Hash receiverAddressHash = tokenMintingFeeBaseTransactionData.getServiceData().getReceiverAddress();
            Optional<BaseTransactionData> identicalAddresses = transactionData.getBaseTransactions().stream().filter(t -> t.getAddressHash().equals(receiverAddressHash)).findFirst();
            if (!identicalAddresses.isPresent()) {
                updateAddressTransactionsHistories(receiverAddressHash, transactionData);
            }
        }
    }
}
