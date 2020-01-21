package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.UnconfirmedReceivedTransactionHashData;
import io.coti.basenode.model.UnconfirmedReceivedTransactionHashes;
import io.coti.basenode.services.BaseNodeTransactionCuratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TransactionCuratorService extends BaseNodeTransactionCuratorService {

    @Autowired
    protected UnconfirmedReceivedTransactionHashes unconfirmedReceivedTransactionHashes;
    @Autowired
    private IPropagationPublisher propagationPublisher;

    public static final long PERIOD_IN_SECONDS_BEFORE_PROPAGATE_AGAIN_DSP_NODE = 180;
    public static final int NUMBER_OF_RETRIES_DSP_NODE = 3;

    @Override
    public void init() {
        unconfirmedReceivedTransactionHashesMap = new ConcurrentHashMap<>();
        updateRecoveredUnconfirmedReceivedTransactions();
        super.init();
    }

    private void updateRecoveredUnconfirmedReceivedTransactions() {
        List<Hash> confirmedReceiptTransactions = new ArrayList<>();
        unconfirmedReceivedTransactionHashes.forEach(unconfirmedReceivedTransactionHashData -> {
            Hash transactionHash = unconfirmedReceivedTransactionHashData.getTransactionHash();
            synchronized (addLockToLockMap(transactionHash)) {
                if (isTransactionHashDSPConfirmed(transactionHash)) {
                    confirmedReceiptTransactions.add(transactionHash);
                } else {
                    unconfirmedReceivedTransactionHashesMap.put(transactionHash, unconfirmedReceivedTransactionHashData);
                }
            }
            removeLockFromLocksMap(transactionHash);
        });
        confirmedReceiptTransactions.forEach(confirmedTransactionHash ->
                unconfirmedReceivedTransactionHashes.deleteByHash(confirmedTransactionHash)
        );
    }

    @Override
    public void addUnconfirmedTransaction(Hash transactionHash) {
        synchronized (addLockToLockMap(transactionHash)) {
            unconfirmedReceivedTransactionHashesMap.put(transactionHash, new UnconfirmedReceivedTransactionHashData(transactionHash, NUMBER_OF_RETRIES_DSP_NODE));
            unconfirmedReceivedTransactionHashes.put(new UnconfirmedReceivedTransactionHashData(transactionHash, NUMBER_OF_RETRIES_DSP_NODE));
        }
        removeLockFromLocksMap(transactionHash);
    }

    @Override
    public void removeConfirmedReceiptTransaction(Hash transactionHash) {
        if (unconfirmedReceivedTransactionHashesMap.containsKey(transactionHash)) {
            doRemoveConfirmedReceiptTransaction(transactionHash);
        }
    }

    private void doRemoveConfirmedReceiptTransaction(Hash transactionHash) {
        synchronized (addLockToLockMap(transactionHash)) {
            unconfirmedReceivedTransactionHashesMap.remove(transactionHash);
            unconfirmedReceivedTransactionHashes.deleteByHash(transactionHash);
        }
        removeLockFromLocksMap(transactionHash);
    }

    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    private void propagateUnconfirmedReceivedTransactions() {

//        if (!unconfirmedReceivedTransactionHashesMap.isEmpty()) {
//            unconfirmedReceivedTransactionHashesMap.entrySet().stream().forEach(entry -> log.warn(entry.getKey().toString()));
//        }   // todo delete after tests

        unconfirmedReceivedTransactionHashesMap
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().getCreatedTime().plusSeconds(PERIOD_IN_SECONDS_BEFORE_PROPAGATE_AGAIN_DSP_NODE).isAfter(Instant.now()))
                .forEach(entry -> {
                    synchronized (addLockToLockMap(entry.getKey())) {
                        TransactionData transactionData = transactions.getByHash(entry.getKey());
                        if (transactionData == null) {
                            entry.getValue().setRetries(0);
                        } else {
                            propagationPublisher.propagate(transactionData, Arrays.asList(
                                    NodeType.FullNode,
                                    NodeType.TrustScoreNode,
                                    NodeType.DspNode,
                                    NodeType.ZeroSpendServer,
                                    NodeType.FinancialServer,
                                    NodeType.HistoryNode));
                            entry.getValue().setRetries(entry.getValue().getRetries() - 1);
                        }
                    }
                    removeLockFromLocksMap(entry.getKey());
                });
        List<Hash> unconfirmedTransactionsToRemove = unconfirmedReceivedTransactionHashesMap
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().getRetries() <= 0)
                .map(entry -> entry.getKey())
                .collect(Collectors.toList());

        unconfirmedTransactionsToRemove.forEach(transactionHash -> doRemoveConfirmedReceiptTransaction(transactionHash));
    }
}
