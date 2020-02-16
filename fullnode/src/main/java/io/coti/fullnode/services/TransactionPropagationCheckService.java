package io.coti.fullnode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeTransactionPropagationCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransactionPropagationCheckService extends BaseNodeTransactionPropagationCheckService {

    @Autowired
    protected NetworkService networkService;

    private static final long PERIOD_IN_SECONDS_BEFORE_PROPAGATE_AGAIN_FULL_NODE = 60;
    private static final int NUMBER_OF_RETRIES_FULL_NODE = 3;

    @Override
    public void init() {
        super.init();
        updateRecoveredUnconfirmedReceivedTransactions();
    }

    @Override
    public void addUnconfirmedTransaction(Hash transactionHash) {
        addUnconfirmedTransaction(transactionHash, NUMBER_OF_RETRIES_FULL_NODE);
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
    public void sendUnconfirmedReceivedTransactions(TransactionData transactionData) {
        networkService.sendDataToConnectedDspNodes(transactionData);
    }
}