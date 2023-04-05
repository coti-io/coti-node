package io.coti.fullnode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.services.BaseNodeTransactionPropagationCheckService;
import io.coti.fullnode.data.UnconfirmedReceivedTransactionHashFullNodeData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static io.coti.fullnode.services.NodeServiceManager.*;

@Slf4j
@Service
@Primary
public class TransactionPropagationCheckService extends BaseNodeTransactionPropagationCheckService {

    private static final long PERIOD_IN_SECONDS_BEFORE_PROPAGATE_AGAIN_FULL_NODE = 60;
    private static final int NUMBER_OF_RETRIES_FULL_NODE = 5;

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
            synchronized (transactionHashLockData.addLockToLockMap(transactionHash)) {
                UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashData = new UnconfirmedReceivedTransactionHashData(transactionHash);
                putToUnconfirmedReceivedTransactionHashesMap(unconfirmedReceivedTransactionHashData);
                unconfirmedReceivedTransactionHashes.put(unconfirmedReceivedTransactionHashData);
            }
        } finally {
            transactionHashLockData.removeLockFromLocksMap(transactionHash);
        }
    }

    @Override
    public void removeConfirmedReceiptTransaction(Hash transactionHash) {
        try {
            synchronized (transactionHashLockData.addLockToLockMap(transactionHash)) {
                unconfirmedReceivedTransactionHashesMap.remove(transactionHash);
                unconfirmedReceivedTransactionHashes.deleteByHash(transactionHash);
            }
        } finally {
            transactionHashLockData.removeLockFromLocksMap(transactionHash);
        }
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 30000)
    private void sendUnconfirmedReceivedTransactionsFullNode() {
        if (networkService.isNotConnectedToDspNodes()) {
            log.error("FullNode is not connected to any DspNode. Failed to send unconfirmed transactions.");
            return;
        }
        unconfirmedReceivedTransactionHashesMap
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().getCreatedTime().plusSeconds(PERIOD_IN_SECONDS_BEFORE_PROPAGATE_AGAIN_FULL_NODE).isBefore(Instant.now()))
                .forEach(this::sendUnconfirmedReceivedTransactionsFullNode);
        List<Hash> unconfirmedTransactionsToRemove = new ArrayList<>();
        List<Hash> unconfirmedTransactionsToRemoveFromMap = new ArrayList<>();
        unconfirmedReceivedTransactionHashesMap.forEach((key, value) -> {
            if (((UnconfirmedReceivedTransactionHashFullNodeData) value).getRetries() < 0) {
                unconfirmedTransactionsToRemove.add(key);
            } else if (((UnconfirmedReceivedTransactionHashFullNodeData) value).getRetries() == 0) {
                unconfirmedTransactionsToRemoveFromMap.add(key);
            }
        });
        unconfirmedTransactionsToRemove.forEach(this::removeConfirmedReceiptTransaction);
        unconfirmedTransactionsToRemoveFromMap.forEach(this::removeUnconfirmedReceivedTransactionsFromHashesMap);
    }

    private void sendUnconfirmedReceivedTransactionsFullNode(Map.Entry<Hash, UnconfirmedReceivedTransactionHashData> entry) {
        Hash transactionHash = entry.getKey();
        UnconfirmedReceivedTransactionHashFullNodeData unconfirmedReceivedTransactionHashFullnodeData = (UnconfirmedReceivedTransactionHashFullNodeData) entry.getValue();
        try {

            synchronized (transactionHashLockData.addLockToLockMap(transactionHash)) {
                TransactionData transactionData = transactions.getByHash(entry.getKey());
                if (transactionData == null) {
                    unconfirmedReceivedTransactionHashFullnodeData.setRetries(-1);
                } else {
                    log.info("Sending unconfirmed transaction {}", transactionData.getHash());
                    sendUnconfirmedReceivedTransactionsFullNode(transactionData, unconfirmedReceivedTransactionHashFullnodeData);
                    unconfirmedReceivedTransactionHashFullnodeData.setRetries(unconfirmedReceivedTransactionHashFullnodeData.getRetries() - 1);
                }
            }
        } finally {
            transactionHashLockData.removeLockFromLocksMap(transactionHash);
        }
    }

    private void sendUnconfirmedReceivedTransactionsFullNode(TransactionData transactionData,
                                                             UnconfirmedReceivedTransactionHashFullNodeData unconfirmedReceivedTransactionHashFullnodeData) {
        handleSenderReconnect(unconfirmedReceivedTransactionHashFullnodeData);
        networkService.sendDataToConnectedDspNodes(transactionData);
    }

    private void handleSenderReconnect(UnconfirmedReceivedTransactionHashFullNodeData unconfirmedReceivedTransactionHashFullnodeData) {
        if (unconfirmedReceivedTransactionHashFullnodeData.getRetries() != 3) {
            return;
        }
        List<NetworkNodeData> connectedDspNodes = new ArrayList<>(networkService.getMapFromFactory(NodeType.DspNode).values());
        for (NetworkNodeData connectedDspNode : connectedDspNodes) {
            communicationService.reconnectSender(connectedDspNode.getReceivingFullAddress(), NodeType.DspNode);
        }
    }

    @Override
    public int getMaximumNumberOfRetries() {
        AtomicInteger maxRetries = new AtomicInteger(NUMBER_OF_RETRIES_FULL_NODE);
        unconfirmedReceivedTransactionHashes.forEach(unconfirmedReceivedTransaction -> {
            UnconfirmedReceivedTransactionHashFullNodeData unconfirmedReceivedTransactionHashDspNodeData =
                    (UnconfirmedReceivedTransactionHashFullNodeData) unconfirmedReceivedTransactionHashesMap.get(unconfirmedReceivedTransaction.getHash());
            if (unconfirmedReceivedTransactionHashDspNodeData == null) {
                maxRetries.set(0);
            } else {
                int retries = unconfirmedReceivedTransactionHashDspNodeData.getRetries();
                if (retries >= 0 && retries < maxRetries.get()) {
                    maxRetries.set(retries);
                }
            }
        });
        return NUMBER_OF_RETRIES_FULL_NODE - maxRetries.get();
    }
}
