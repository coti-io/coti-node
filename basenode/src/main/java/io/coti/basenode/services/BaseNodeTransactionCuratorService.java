package io.coti.basenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.UnconfirmedReceivedTransactionHashData;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.ITransactionCuratorService;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class BaseNodeTransactionCuratorService implements ITransactionCuratorService {

    @Autowired
    protected Transactions transactions;
    @Autowired
    private ITransactionHelper transactionHelper;

    protected Map<Hash, UnconfirmedReceivedTransactionHashData> unconfirmedReceivedTransactionHashesMap;
    protected Map<Hash, Hash> lockVotedTransactionRecordHashMap = new ConcurrentHashMap<>();

    @Override
    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    protected boolean isTransactionHashDSPConfirmed(Hash transactionHash) {
        TransactionData transactionData = transactions.getByHash(transactionHash);
        if (transactionData != null) {
            return transactionHelper.isDspConfirmed(transactionData);
        }
        return false;
    }

    @Override
    public void addUnconfirmedTransaction(Hash transactionHash) {
        // implemented for full nodes and dsp nodes
    }

    @Override
    public void removeConfirmedReceiptTransaction(Hash transactionHash) {
        // implemented for full nodes and dsp nodes
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
