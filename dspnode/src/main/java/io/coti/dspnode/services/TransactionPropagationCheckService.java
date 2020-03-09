package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.data.*;
import io.coti.basenode.services.BaseNodeTransactionPropagationCheckService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.dspnode.data.UnconfirmedReceivedTransactionHashDspData;
import io.coti.dspnode.model.UnconfirmedTransactionDspVotes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
    public void updateRecoveredUnconfirmedReceivedTransactions() {
        List<Hash> confirmedReceiptTransactions = new ArrayList<>();
        unconfirmedReceivedTransactionHashes.forEach(unconfirmedReceivedTransactionHashData -> {
            Hash transactionHash = ((UnconfirmedReceivedTransactionHashDspData) unconfirmedReceivedTransactionHashData).getTransactionHash();
            if (isTransactionHashDSPConfirmed(transactionHash)) {
                confirmedReceiptTransactions.add(transactionHash);
            } else {
                unconfirmedReceivedTransactionHashesMap.put(transactionHash, (UnconfirmedReceivedTransactionHashData) unconfirmedReceivedTransactionHashData);
            }
        });
        confirmedReceiptTransactions.forEach(confirmedTransactionHash -> {
            unconfirmedReceivedTransactionHashes.deleteByHash(confirmedTransactionHash);
            removeConfirmedReceiptTransactionDSPVote(confirmedTransactionHash);
        });
    }

    public void addUnconfirmedTransaction(Hash transactionHash, boolean dSPVoteOnly) {
        UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashData =
                new UnconfirmedReceivedTransactionHashDspData(transactionHash, NUMBER_OF_RETRIES_DSP_NODE, dSPVoteOnly);
        addUnconfirmedTransaction(transactionHash, unconfirmedReceivedTransactionHashData);
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

    @Override
    protected <T extends UnconfirmedReceivedTransactionHashData> void sendUnconfirmedReceivedTransactions(Map.Entry<Hash, T> entry) {
        try {
            synchronized (addLockToLockMap(entry.getKey())) {
                TransactionData transactionData = transactions.getByHash(entry.getKey());
                if (transactionData == null) {
                    entry.getValue().setRetries(0);
                } else {
                    sendUnconfirmedReceivedTransactions(transactionData, ((UnconfirmedReceivedTransactionHashDspData) entry.getValue()).isDSPVoteOnly());
                    entry.getValue().setRetries(entry.getValue().getRetries() - 1);
                }
            }
        } finally {
            removeLockFromLocksMap(entry.getKey());
        }
    }
}