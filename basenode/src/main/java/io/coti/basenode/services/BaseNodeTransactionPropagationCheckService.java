package io.coti.basenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionDspVote;
import io.coti.basenode.data.UnconfirmedReceivedTransactionHashData;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.model.UnconfirmedReceivedTransactionHashes;
import io.coti.basenode.services.interfaces.ITransactionHelper;
import io.coti.basenode.services.interfaces.ITransactionPropagationCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class BaseNodeTransactionPropagationCheckService implements ITransactionPropagationCheckService {

    @Autowired
    protected Transactions transactions;
    @Autowired
    private ITransactionHelper transactionHelper;
    @Autowired
    protected UnconfirmedReceivedTransactionHashes unconfirmedReceivedTransactionHashes;
    protected Map<Hash, UnconfirmedReceivedTransactionHashData> unconfirmedReceivedTransactionHashesMap;
    protected Map<Hash, Hash> lockVotedTransactionRecordHashMap = new ConcurrentHashMap<>();
    private final Object lock = new Object();

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
    public void recoverUnconfirmedReceivedTransactions() {
        List<Hash> confirmedReceiptTransactions = new ArrayList<>();
        unconfirmedReceivedTransactionHashes.forEach(unconfirmedReceivedTransactionHashData -> {
            Hash transactionHash = unconfirmedReceivedTransactionHashData.getTransactionHash();
            if (isTransactionHashDSPConfirmed(transactionHash)) {
                confirmedReceiptTransactions.add(transactionHash);
            } else {
                putNewUnconfirmedTransaction(unconfirmedReceivedTransactionHashData);
            }
        });
        confirmedReceiptTransactions.forEach(confirmedTransactionHash -> {
            unconfirmedReceivedTransactionHashes.deleteByHash(confirmedTransactionHash);
            removeConfirmedReceiptTransactionDSPVote(confirmedTransactionHash);
        });
    }

    @Override
    public void putNewUnconfirmedTransaction(UnconfirmedReceivedTransactionHashData unconfirmedReceivedTransactionHashData) {
        // implemented for full nodes and dsp nodes
    }

    @Override
    public void addNewUnconfirmedTransaction(Hash transactionHash) {
        // implemented for full nodes and dsp nodes
    }

    @Override
    public void addPropagatedUnconfirmedTransaction(Hash transactionHash) {
        // implemented for dsp nodes
    }

    @Override
    public void addUnconfirmedTransactionDSPVote(TransactionDspVote transactionDspVote) {
        // implemented for dsp nodes
    }

    @Override
    public void removeTransactionHashFromUnconfirmed(Hash transactionHash) {
        // implemented for full nodes and dsp nodes
    }

    protected void removeTransactionHashFromUnconfirmedTransaction(Hash transactionHash) {
        if (unconfirmedReceivedTransactionHashesMap.containsKey(transactionHash)) {
            removeConfirmedReceiptTransaction(transactionHash);
        }
    }

    protected void removeConfirmedReceiptTransaction(Hash transactionHash) {
        synchronized (addLockToLockMap(transactionHash)) {
            unconfirmedReceivedTransactionHashesMap.remove(transactionHash);
            unconfirmedReceivedTransactionHashes.deleteByHash(transactionHash);
            removeConfirmedReceiptTransactionDSPVote(transactionHash);
        }
        removeLockFromLocksMap(transactionHash);
    }

    @Override
    public void removeConfirmedReceiptTransactionDSPVote(Hash transactionHash) {
        // implemented for dsp nodes
    }


    @Override
    public void removeTransactionHashFromUnconfirmedOnBackPropagation(Hash transactionHash) {
        // implemented for full nodes
    }

    protected Hash addLockToLockMap(Hash hash) {
        synchronized (lock) {
            lockVotedTransactionRecordHashMap.putIfAbsent(hash, hash);
            return lockVotedTransactionRecordHashMap.get(hash);
        }
    }

    protected void removeLockFromLocksMap(Hash hash) {
        synchronized (lock) {
            lockVotedTransactionRecordHashMap.remove(hash);
        }
    }

}
