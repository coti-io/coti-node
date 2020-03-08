package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionDspVote;
import io.coti.basenode.services.BaseNodeTransactionPropagationCheckService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.dspnode.model.UnconfirmedTransactionDspVotes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TransactionPropagationCheckService extends BaseNodeTransactionPropagationCheckService {

    private static final long PERIOD_IN_SECONDS_BEFORE_PROPAGATE_AGAIN_DSP_NODE = 60;
    private static final int NUMBER_OF_RETRIES_DSP_NODE = 3;
    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private ISender sender;
    @Autowired
    private INetworkService networkService;
    @Autowired
    private UnconfirmedTransactionDspVotes unconfirmedTransactionDspVotes;

    @Override
    public void init() {
        super.init();
        unconfirmedReceivedTransactionHashesMap = new ConcurrentHashMap<>();
        updateRecoveredUnconfirmedReceivedTransactions();
    }

    @Override
    public void addUnconfirmedTransaction(Hash transactionHash, boolean dSPVoteOnly) {
        addUnconfirmedTransaction(transactionHash, NUMBER_OF_RETRIES_DSP_NODE, dSPVoteOnly);
    }

    @Override
    public void addUnconfirmedTransactionDSPVote(TransactionDspVote transactionDspVote) {
        Hash transactionHash = transactionDspVote.getTransactionHash();
        try {
            synchronized (addLockToLockMap(transactionHash)) {
                if (unconfirmedReceivedTransactionHashesMap.containsKey(transactionHash)) {
                    unconfirmedTransactionDspVotes.put(transactionDspVote);
                }
            }
        } finally {
            removeLockFromLocksMap(transactionHash);
        }
    }

    @Override
    public void removeTransactionHashFromUnconfirmed(Hash transactionHash) {
        removeTransactionHashFromUnconfirmedTransaction(transactionHash);
    }

    @Override
    public void removeConfirmedReceiptTransactionDSPVote(Hash transactionHash) {
        unconfirmedTransactionDspVotes.deleteByHash(transactionHash);
    }

    @Scheduled(initialDelay = 60000, fixedDelay = 60000)
    private void propagateUnconfirmedReceivedTransactions() {
        sendUnconfirmedReceivedTransactions(PERIOD_IN_SECONDS_BEFORE_PROPAGATE_AGAIN_DSP_NODE);
    }

    @Override
    public void sendUnconfirmedReceivedTransactions(TransactionData transactionData, boolean dSPVoteOnly) {
        TransactionDspVote transactionDspVote = unconfirmedTransactionDspVotes.getByHash(transactionData.getHash());
        if (transactionDspVote != null) {
            String zerospendReceivingAddress = networkService.getSingleNodeData(NodeType.ZeroSpendServer).getReceivingFullAddress();
            sender.send(transactionDspVote, zerospendReceivingAddress);
        }

        if (!dSPVoteOnly) {
            propagationPublisher.propagate(transactionData, Arrays.asList(
                    NodeType.FullNode,
                    NodeType.TrustScoreNode,
                    NodeType.DspNode,
                    NodeType.ZeroSpendServer,
                    NodeType.FinancialServer,
                    NodeType.HistoryNode));
        }

    }
}