package io.coti.fullnode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.UnconfirmedReceivedTransactionHashData;
import io.coti.basenode.services.BaseNodeTransactionPropagationCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        updateRecoveredUnconfirmedReceivedTransactions();
    }

    @Override
    public void updateRecoveredUnconfirmedReceivedTransactions() {
        List<Hash> confirmedReceiptTransactions = new ArrayList<>();
        unconfirmedReceivedTransactionHashes.forEach(unconfirmedReceivedTransactionHashData -> {
            Hash transactionHash = ((UnconfirmedReceivedTransactionHashData) unconfirmedReceivedTransactionHashData).getTransactionHash();
            unconfirmedReceivedTransactionHashesMap.put(transactionHash, (UnconfirmedReceivedTransactionHashData) unconfirmedReceivedTransactionHashData);
        });
        confirmedReceiptTransactions.forEach(confirmedTransactionHash -> {
            unconfirmedReceivedTransactionHashes.deleteByHash(confirmedTransactionHash);
            removeConfirmedReceiptTransactionDSPVote(confirmedTransactionHash);
        });
    }

    public void addUnconfirmedTransaction(Hash transactionHash) {
        addUnconfirmedTransaction(transactionHash, NUMBER_OF_RETRIES_FULL_NODE);
    }

    @Override
    public void removeTransactionHashFromUnconfirmed(Hash transactionHash) {
        removeTransactionHashFromUnconfirmedTransaction(transactionHash);
    }

    @Override
    public void removeTransactionHashFromUnconfirmedOnBackPropagation(Hash transactionHash) {
        removeTransactionHashFromUnconfirmed(transactionHash);
    }

    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    private void sendUnconfirmedReceivedTransactions() {
        sendUnconfirmedReceivedTransactions(PERIOD_IN_SECONDS_BEFORE_PROPAGATE_AGAIN_FULL_NODE);
    }

    @Override
    protected <T extends UnconfirmedReceivedTransactionHashData> void sendUnconfirmedReceivedTransactions(Map.Entry<Hash, T> entry) {
        try {
            synchronized (addLockToLockMap(entry.getKey())) {
                TransactionData transactionData = transactions.getByHash(entry.getKey());
                if (transactionData == null) {
                    entry.getValue().setRetries(0);
                } else {
                    networkService.sendDataToConnectedDspNodes(transactionData);
                    entry.getValue().setRetries(entry.getValue().getRetries() - 1);
                }
            }
        } finally {
            removeLockFromLocksMap(entry.getKey());
        }
    }
}