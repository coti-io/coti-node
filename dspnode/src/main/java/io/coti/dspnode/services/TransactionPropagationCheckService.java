package io.coti.dspnode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.services.BaseNodeTransactionPropagationCheckService;
import io.coti.dspnode.data.UnconfirmedReceivedTransactionHashDspNodeData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static io.coti.dspnode.services.NodeServiceManager.*;

@Slf4j
@Service
@Primary
public class TransactionPropagationCheckService extends BaseNodeTransactionPropagationCheckService {

    private static final long PERIOD_IN_SECONDS_BEFORE_PROPAGATE_AGAIN_DSP_NODE = 60;
    private static final int NUMBER_OF_RETRIES_DSP_NODE = 5;

    @Override
    public void init() {
        super.init();
        unconfirmedReceivedTransactionHashesMap = new ConcurrentHashMap<>();
        recoverUnconfirmedReceivedTransactions();
        recoverUnconfirmedPropagatedTransactions();
    }

    private void recoverUnconfirmedPropagatedTransactions() {
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
    public void removeConfirmedReceivedTransactions(List<Hash> confirmedReceiptTransactions) {
        confirmedReceiptTransactions.forEach(confirmedTransactionHash -> {
            unconfirmedReceivedTransactionHashes.deleteByHash(confirmedTransactionHash);
            removeConfirmedReceiptTransactionDSPVote(confirmedTransactionHash);
        });
    }

    @Override
    public void putNewUnconfirmedTransaction(UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashData) {
        putToUnconfirmedReceivedTransactionHashesMap(unconfirmedReceivedTransactionHashData, false);
    }

    private void putNewUnconfirmedVote(TransactionDspVote transactionDspVote) {
        UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashData = new UnconfirmedReceivedTransactionHashData(transactionDspVote.getTransactionHash());
        putToUnconfirmedReceivedTransactionHashesMap(unconfirmedReceivedTransactionHashData, true);
    }

    private void putToUnconfirmedReceivedTransactionHashesMap(UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashData, boolean dspVoteOnly) {
        UnconfirmedReceivedTransactionHashDspNodeData unconfirmedReceivedTransactionHashDspNodeData =
                new UnconfirmedReceivedTransactionHashDspNodeData(unconfirmedReceivedTransactionHashData, NUMBER_OF_RETRIES_DSP_NODE, dspVoteOnly);
        unconfirmedReceivedTransactionHashesMap.put(unconfirmedReceivedTransactionHashData.getTransactionHash(), unconfirmedReceivedTransactionHashDspNodeData);
    }

    @Override
    public void addNewUnconfirmedTransaction(Hash transactionHash) {
        addUnconfirmedTransaction(transactionHash, false);
    }

    @Override
    public void addPropagatedUnconfirmedTransaction(Hash transactionHash) {
        addUnconfirmedTransaction(transactionHash, true);
    }

    private void addUnconfirmedTransaction(Hash transactionHash, boolean dspVoteOnly) {
        try {
            synchronized (transactionHashLockData.addLockToLockMap(transactionHash)) {
                UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashData = new UnconfirmedReceivedTransactionHashData(transactionHash);
                putToUnconfirmedReceivedTransactionHashesMap(unconfirmedReceivedTransactionHashData, dspVoteOnly);
                if (!dspVoteOnly) {
                    unconfirmedReceivedTransactionHashes.put(unconfirmedReceivedTransactionHashData);
                }
            }
        } finally {
            transactionHashLockData.removeLockFromLocksMap(transactionHash);
        }
    }

    @Override
    public void addUnconfirmedTransactionDSPVote(TransactionDspVote transactionDspVote) {
        Hash transactionHash = transactionDspVote.getTransactionHash();
        try {
            synchronized (transactionHashLockData.addLockToLockMap(transactionHash)) {
                if (unconfirmedReceivedTransactionHashesMap.containsKey(transactionHash)) {
                    unconfirmedTransactionDspVotes.put(transactionDspVote);
                }
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
                removeConfirmedReceiptTransactionDSPVote(transactionHash);
            }
        } finally {
            transactionHashLockData.removeLockFromLocksMap(transactionHash);
        }
    }

    private void removeConfirmedReceiptTransactionDSPVote(Hash transactionHash) {
        unconfirmedTransactionDspVotes.deleteByHash(transactionHash);
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 30000)
    private void propagateUnconfirmedReceivedTransactions() {
        if (!networkService.isZeroSpendServerInNetwork()) {
            log.error("ZeroSpendServer is not in the network. Failed to send unconfirmed transactions.");
            return;
        }
        unconfirmedReceivedTransactionHashesMap
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().getCreatedTime().plusSeconds(PERIOD_IN_SECONDS_BEFORE_PROPAGATE_AGAIN_DSP_NODE).isBefore(Instant.now()))
                .forEach(this::sendUnconfirmedReceivedTransactionsDSP);
        List<Hash> unconfirmedTransactionsToRemove = unconfirmedReceivedTransactionHashesMap
                .entrySet()
                .stream()
                .filter(entry -> ((UnconfirmedReceivedTransactionHashDspNodeData) entry.getValue()).getRetries() <= 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        unconfirmedTransactionsToRemove.forEach(this::removeConfirmedReceiptTransaction);
    }

    private void sendUnconfirmedReceivedTransactionsDSP(Map.Entry<Hash, UnconfirmedReceivedTransactionHashData> entry) {
        Hash transactionHash = entry.getKey();
        UnconfirmedReceivedTransactionHashDspNodeData unconfirmedReceivedTransactionHashDspNodeData = (UnconfirmedReceivedTransactionHashDspNodeData) entry.getValue();
        try {

            synchronized (transactionHashLockData.addLockToLockMap(transactionHash)) {
                TransactionData transactionData = transactions.getByHash(transactionHash);
                if (transactionData == null) {
                    unconfirmedReceivedTransactionHashDspNodeData.setRetries(0);
                } else {
                    sendUnconfirmedReceivedTransactionsDSP(transactionData, unconfirmedReceivedTransactionHashDspNodeData);
                    unconfirmedReceivedTransactionHashDspNodeData.setRetries(unconfirmedReceivedTransactionHashDspNodeData.getRetries() - 1);
                }
            }
        } finally {
            transactionHashLockData.removeLockFromLocksMap(transactionHash);
        }
    }

    private void sendUnconfirmedReceivedTransactionsDSP(TransactionData transactionData, UnconfirmedReceivedTransactionHashDspNodeData dspNodeData) {
        if (!dspNodeData.isDspVoteOnly()) {
            log.info("Sending unconfirmed transaction {} to ZeroSpendServer", transactionData.getHash());
            propagationPublisher.propagate(transactionData, Arrays.asList(
                    NodeType.FullNode,
                    NodeType.TrustScoreNode,
                    NodeType.DspNode,
                    NodeType.ZeroSpendServer,
                    NodeType.FinancialServer,
                    NodeType.HistoryNode));
        }

        TransactionDspVote transactionDspVote = unconfirmedTransactionDspVotes.getByHash(transactionData.getHash());
        if (transactionDspVote != null) {
            log.info("Sending dsp vote for transaction {} to ZeroSpendServer", transactionData.getHash());
            String zeroSpendReceivingAddress = networkService.getSingleNodeData(NodeType.ZeroSpendServer).getReceivingFullAddress();
            handleSenderReconnect(zeroSpendReceivingAddress, dspNodeData);
            zeroMQSender.send(transactionDspVote, zeroSpendReceivingAddress);
        }
    }

    private void handleSenderReconnect(String zeroSpendReceivingAddress, UnconfirmedReceivedTransactionHashDspNodeData dspNodeData) {
        if (dspNodeData.getRetries() > 3) {
            return;
        }
        communicationService.removeSender(zeroSpendReceivingAddress, NodeType.ZeroSpendServer);
        communicationService.addSender(zeroSpendReceivingAddress, NodeType.ZeroSpendServer);
    }
}
