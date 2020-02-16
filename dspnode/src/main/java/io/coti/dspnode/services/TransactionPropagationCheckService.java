package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeTransactionPropagationCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
public class TransactionPropagationCheckService extends BaseNodeTransactionPropagationCheckService {

    @Autowired
    private IPropagationPublisher propagationPublisher;

    private static final long PERIOD_IN_SECONDS_BEFORE_PROPAGATE_AGAIN_DSP_NODE = 60;
    private static final int NUMBER_OF_RETRIES_DSP_NODE = 3;

    @Override
    public void init() {
        super.init();
        updateRecoveredUnconfirmedReceivedTransactions();
    }

    @Override
    public void addUnconfirmedTransaction(Hash transactionHash) {
        addUnconfirmedTransaction(transactionHash, NUMBER_OF_RETRIES_DSP_NODE);
    }

    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    private void propagateUnconfirmedReceivedTransactions() {
        sendUnconfirmedReceivedTransactions(PERIOD_IN_SECONDS_BEFORE_PROPAGATE_AGAIN_DSP_NODE);
    }

    @Override
    public void sendUnconfirmedReceivedTransactions(TransactionData transactionData) {
        propagationPublisher.propagate(transactionData, Arrays.asList(
                NodeType.FullNode,
                NodeType.TrustScoreNode,
                NodeType.DspNode,
                NodeType.ZeroSpendServer,
                NodeType.FinancialServer,
                NodeType.HistoryNode));
    }
}