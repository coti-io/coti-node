package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.data.*;
import io.coti.basenode.services.BaseNodeTransactionPropagationCheckService;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.dspnode.data.UnconfirmedReceivedTransactionHashDSPData;
import io.coti.dspnode.model.UnconfirmedTransactionDspVotes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TransactionPropagationCheckService extends BaseNodeTransactionPropagationCheckService {

    private static final long PERIOD_IN_SECONDS_BEFORE_PROPAGATE_AGAIN_DSP_NODE = 60;
    private static final int NUMBER_OF_RETRIES_DSP_NODE = 5;
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
        updateRecoveredUnconfirmedPropagatedTransactions();
    }

    public void updateRecoveredUnconfirmedPropagatedTransactions() {
        List<Hash> confirmedReceiptTransactions = new ArrayList<>();
        unconfirmedTransactionDspVotes.forEach(transactionDspVote -> {
            Hash transactionHash = transactionDspVote.getTransactionHash();
            if (!unconfirmedReceivedTransactionHashesMap.containsKey(transactionHash)) {
                if (isTransactionHashDSPConfirmed(transactionHash)) {
                    confirmedReceiptTransactions.add(transactionHash);
                } else {
                    putNewUnconfirmedVote(transactionDspVote);
                }
            }
        });
        confirmedReceiptTransactions.forEach(this::removeConfirmedReceiptTransactionDSPVote);
    }

    @Override
    public void putNewUnconfirmedTransaction(UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashData) {
        UnconfirmedReceivedTransactionHashDSPData unconfirmedReceivedTransactionHashDSPData =
                new UnconfirmedReceivedTransactionHashDSPData(unconfirmedReceivedTransactionHashData, NUMBER_OF_RETRIES_DSP_NODE);
        unconfirmedReceivedTransactionHashesMap.put(unconfirmedReceivedTransactionHashData.getTransactionHash(), unconfirmedReceivedTransactionHashDSPData);
    }

    private void putNewUnconfirmedVote(TransactionDspVote transactionDspVote) {
        UnconfirmedReceivedTransactionHashDSPData unconfirmedReceivedTransactionHashDSPData =
                new UnconfirmedReceivedTransactionHashDSPData(transactionDspVote.getTransactionHash(), NUMBER_OF_RETRIES_DSP_NODE, true);
        unconfirmedReceivedTransactionHashesMap.put(transactionDspVote.getTransactionHash(), unconfirmedReceivedTransactionHashDSPData);
    }

    @Override
    public void addNewUnconfirmedTransaction(Hash transactionHash) {
        doAddUnconfirmedTransaction(transactionHash, false);
    }

    @Override
    public void addPropagatedUnconfirmedTransaction(Hash transactionHash) {
        doAddUnconfirmedTransaction(transactionHash, true);
    }

    private void doAddUnconfirmedTransaction(Hash transactionHash, boolean dSPVoteOnly) {
        UnconfirmedReceivedTransactionHashDSPData unconfirmedReceivedTransactionHashDSPData =
                new UnconfirmedReceivedTransactionHashDSPData(transactionHash, NUMBER_OF_RETRIES_DSP_NODE, dSPVoteOnly);
        try {
            synchronized (addLockToLockMap(transactionHash)) {
                unconfirmedReceivedTransactionHashesMap.put(transactionHash, unconfirmedReceivedTransactionHashDSPData);
                if (!dSPVoteOnly) {
                    unconfirmedReceivedTransactionHashes.put(unconfirmedReceivedTransactionHashDSPData);
                }
            }
        } finally {
            removeLockFromLocksMap(transactionHash);
        }
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
        unconfirmedReceivedTransactionHashesMap
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().getCreatedTime().plusSeconds(PERIOD_IN_SECONDS_BEFORE_PROPAGATE_AGAIN_DSP_NODE).isBefore(Instant.now()))
                .forEach(this::sendUnconfirmedReceivedTransactionsDSP);
        List<Hash> unconfirmedTransactionsToRemove = unconfirmedReceivedTransactionHashesMap
                .entrySet()
                .stream()
                .filter(entry -> ((UnconfirmedReceivedTransactionHashDSPData) entry.getValue()).getRetries() <= 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        unconfirmedTransactionsToRemove.forEach(this::doRemoveConfirmedReceiptTransaction);
    }

    private void sendUnconfirmedReceivedTransactionsDSP(Map.Entry<Hash, UnconfirmedReceivedTransactionHashData> entry) {
        try {
            UnconfirmedReceivedTransactionHashDSPData unconfirmedReceivedTransactionHashDSPData = (UnconfirmedReceivedTransactionHashDSPData) entry.getValue();
            synchronized (addLockToLockMap(entry.getKey())) {
                TransactionData transactionData = transactions.getByHash(entry.getKey());
                if (transactionData == null) {
                    unconfirmedReceivedTransactionHashDSPData.setRetries(0);
                } else {
                    sendUnconfirmedReceivedTransactionsDSP(transactionData, unconfirmedReceivedTransactionHashDSPData.isDSPVoteOnly());
                    unconfirmedReceivedTransactionHashDSPData.setRetries(unconfirmedReceivedTransactionHashDSPData.getRetries() - 1);
                }
            }
        } finally {
            removeLockFromLocksMap(entry.getKey());
        }
    }

    public void sendUnconfirmedReceivedTransactionsDSP(TransactionData transactionData, boolean dSPVoteOnly) {
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