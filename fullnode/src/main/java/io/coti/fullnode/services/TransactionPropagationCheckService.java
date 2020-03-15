package io.coti.fullnode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.UnconfirmedReceivedTransactionHashData;
import io.coti.basenode.services.BaseNodeTransactionPropagationCheckService;
import io.coti.fullnode.data.UnconfirmedReceivedTransactionHashFullNodeData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TransactionPropagationCheckService extends BaseNodeTransactionPropagationCheckService {

    private static final long PERIOD_IN_SECONDS_BEFORE_PROPAGATE_AGAIN_FULL_NODE = 60;
    private static final int NUMBER_OF_RETRIES_FULL_NODE = 3;
    @Autowired
    protected NetworkService networkService;

    @Override
    public void init() {
        super.init();
        unconfirmedReceivedTransactionHashesMap = new ConcurrentHashMap<>();
        recoverUnconfirmedReceivedTransactions();
    }

    @Override
    public void removeConfirmedReceivedTransactions(List<Hash> confirmedReceiptTransactions) {
        confirmedReceiptTransactions.forEach(confirmedTransactionHash ->
                unconfirmedReceivedTransactionHashes.deleteByHash(confirmedTransactionHash)
        );
    }

    @Override
    public void putNewUnconfirmedTransaction(UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashData) {
        putToUnconfirmedReceivedTransactionHashesMap(unconfirmedReceivedTransactionHashData);
    }

    private void putToUnconfirmedReceivedTransactionHashesMap(UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashData) {
        UnconfirmedReceivedTransactionHashFullNodeData unconfirmedReceivedTransactionHashFullNodeData =
                new UnconfirmedReceivedTransactionHashFullNodeData(unconfirmedReceivedTransactionHashData, NUMBER_OF_RETRIES_FULL_NODE);
        unconfirmedReceivedTransactionHashesMap.put(unconfirmedReceivedTransactionHashData.getTransactionHash(), unconfirmedReceivedTransactionHashFullNodeData);
    }

    @Override
    public void addNewUnconfirmedTransaction(Hash transactionHash) {
        try {
            synchronized (addLockToLockMap(transactionHash)) {
                UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashData = new UnconfirmedReceivedTransactionHashData(transactionHash);
                putToUnconfirmedReceivedTransactionHashesMap(unconfirmedReceivedTransactionHashData);
                unconfirmedReceivedTransactionHashes.put(unconfirmedReceivedTransactionHashData);
            }
        } finally {
            removeLockFromLockMap(transactionHash);
        }
    }

    @Override
    public void removeConfirmedReceiptTransaction(Hash transactionHash) {
        synchronized (addLockToLockMap(transactionHash)) {
            unconfirmedReceivedTransactionHashesMap.remove(transactionHash);
            unconfirmedReceivedTransactionHashes.deleteByHash(transactionHash);
        }
        removeLockFromLockMap(transactionHash);
    }

    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    private void sendUnconfirmedReceivedTransactionsFullnode() {
        unconfirmedReceivedTransactionHashesMap
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().getCreatedTime().plusSeconds(PERIOD_IN_SECONDS_BEFORE_PROPAGATE_AGAIN_FULL_NODE).isBefore(Instant.now()))
                .forEach(this::sendUnconfirmedReceivedTransactionsFullnode);
        List<Hash> unconfirmedTransactionsToRemove = unconfirmedReceivedTransactionHashesMap
                .entrySet()
                .stream()
                .filter(entry -> ((UnconfirmedReceivedTransactionHashFullNodeData) entry.getValue()).getRetries() <= 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        unconfirmedTransactionsToRemove.forEach(this::removeConfirmedReceiptTransaction);
    }

    private void sendUnconfirmedReceivedTransactionsFullnode(Map.Entry<Hash, UnconfirmedReceivedTransactionHashData> entry) {
        try {
            UnconfirmedReceivedTransactionHashFullNodeData unconfirmedReceivedTransactionHashFullnodeData = (UnconfirmedReceivedTransactionHashFullNodeData) entry.getValue();
            synchronized (addLockToLockMap(entry.getKey())) {
                TransactionData transactionData = transactions.getByHash(entry.getKey());
                if (transactionData == null) {
                    unconfirmedReceivedTransactionHashFullnodeData.setRetries(0);
                } else {
                    sendUnconfirmedReceivedTransactionsFullnode(transactionData);
                    unconfirmedReceivedTransactionHashFullnodeData.setRetries(unconfirmedReceivedTransactionHashFullnodeData.getRetries() - 1);
                }
            }
        } finally {
            removeLockFromLockMap(entry.getKey());
        }
    }

    public void sendUnconfirmedReceivedTransactionsFullnode(TransactionData transactionData) {
        networkService.sendDataToConnectedDspNodes(transactionData);
    }
}