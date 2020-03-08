package io.coti.fullnode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeTransactionPropagationCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
    public void addUnconfirmedTransaction(Hash transactionHash, boolean dSPVoteOnly) {
        addUnconfirmedTransaction(transactionHash, NUMBER_OF_RETRIES_FULL_NODE, dSPVoteOnly);
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
    public void sendUnconfirmedReceivedTransactions(TransactionData transactionData, boolean dSPVoteOnly) {
        networkService.sendDataToConnectedDspNodes(transactionData);
    }
}