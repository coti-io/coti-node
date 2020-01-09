package io.coti.fullnode.services;

import com.dictiography.collections.IndexedNavigableSet;
import com.dictiography.collections.IndexedTreeSet;
import io.coti.basenode.data.DspConsensusResult;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.UnconfirmedReceivedTransactionHashData;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.model.UnconfirmedReceivedTransactionHashes;
import io.coti.basenode.services.BaseNodeUnconfirmedReceivedTransactionService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class UnconfirmedReceivedTransactionService extends BaseNodeUnconfirmedReceivedTransactionService {

    @Autowired
    private UnconfirmedReceivedTransactionHashes unconfirmedReceivedTransactionHashes;
    @Autowired
    private Transactions transactions;
    @Autowired
    private INetworkService networkService;

    private IndexedNavigableSet<UnconfirmedReceivedTransactionHashData> unconfirmedReceivedTransactionHashesSet;
    private Map<Hash, Hash> lockVotedTransactionRecordHashMap = new ConcurrentHashMap<>();
    public static final long PERIOD_IN_SECONDS_BEFORE_PROPAGATE_AGAIN = 60;

    @Override
    public void init() {
        unconfirmedReceivedTransactionHashesSet = new IndexedTreeSet<>();
        updateRecoveredUnconfirmedReceivedTransactions();
        super.init();
    }

    private void updateRecoveredUnconfirmedReceivedTransactions() {
        List<Hash> confirmedReceiptTransactions = new ArrayList<>();
        unconfirmedReceivedTransactionHashes.forEach(unconfirmedReceivedTransactionHashData -> {
            Hash transactionHash = unconfirmedReceivedTransactionHashData.getTransactionHash();
            synchronized (addLockToLockMap(transactionHash)) {
                if (isTransactionReceivedByDSP(transactionHash)) {
                    confirmedReceiptTransactions.add(transactionHash);
                } else {
                    unconfirmedReceivedTransactionHashesSet.add(unconfirmedReceivedTransactionHashData);
                }
            }
            removeLockFromLocksMap(transactionHash);
        });
        confirmedReceiptTransactions.forEach(confirmedTransactionHash ->
                unconfirmedReceivedTransactionHashes.deleteByHash(confirmedTransactionHash)
        );
    }

    private boolean isTransactionReceivedByDSP(Hash transactionHash) {
        TransactionData transactionData = transactions.getByHash(transactionHash);
        if (transactionData != null) {
            DspConsensusResult dspConsensusResult = transactionData.getDspConsensusResult();
            return dspConsensusResult != null && dspConsensusResult.getDspVotes() != null && !dspConsensusResult.getDspVotes().isEmpty();
        }
        return false;
    }

    protected void removeConfirmedReceiptTransaction(Hash transactionHash) {
        synchronized (addLockToLockMap(transactionHash)) {
            unconfirmedReceivedTransactionHashesSet.removeIf(transactionHashData -> transactionHashData.getHash() == transactionHash);
            unconfirmedReceivedTransactionHashes.deleteByHash(transactionHash);
        }
        removeLockFromLocksMap(transactionHash);
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 30000)
    private void propagateUnconfirmedReceivedTransactions() {
        for (Iterator<UnconfirmedReceivedTransactionHashData> iterator = unconfirmedReceivedTransactionHashesSet.iterator(); iterator.hasNext(); ) {
            UnconfirmedReceivedTransactionHashData unconfirmedTransactionHashData = iterator.next();
            Hash unconfirmedTransactionHashDataHash = unconfirmedTransactionHashData.getHash();
            synchronized (addLockToLockMap(unconfirmedTransactionHashDataHash)) {
                if (unconfirmedTransactionHashData.getCreatedTime().plusSeconds(PERIOD_IN_SECONDS_BEFORE_PROPAGATE_AGAIN).isAfter(Instant.now())) {
                    TransactionData transactionData = transactions.getByHash(unconfirmedTransactionHashDataHash);
                    if (transactionData == null) {
                        log.error("Transaction {} attempted to propagate again, but it is not available in the database", unconfirmedTransactionHashDataHash);
                        iterator.remove();
                        unconfirmedReceivedTransactionHashes.deleteByHash(unconfirmedTransactionHashDataHash);
                    } else {
                        ((NetworkService) networkService).sendDataToConnectedDspNodes(transactionData);
                    }
                }
            }
            removeLockFromLocksMap(unconfirmedTransactionHashDataHash);
        }
    }

    protected Hash addLockToLockMap(Hash hash) {
        return addLockToLockMap(lockVotedTransactionRecordHashMap, hash);
    }

    private Hash addLockToLockMap(Map<Hash, Hash> locksIdentityMap, Hash hash) {
        synchronized (locksIdentityMap) {
            locksIdentityMap.putIfAbsent(hash, hash);
            return locksIdentityMap.get(hash);
        }
    }

    protected void removeLockFromLocksMap(Hash hash) {
        removeLockFromLocksMap(lockVotedTransactionRecordHashMap, hash);
    }

    private void removeLockFromLocksMap(Map<Hash, Hash> locksIdentityMap, Hash hash) {
        synchronized (locksIdentityMap) {
            Hash hashLock = locksIdentityMap.get(hash);
            if (hashLock != null) {
                locksIdentityMap.remove(hash);
            }
        }
    }

}
