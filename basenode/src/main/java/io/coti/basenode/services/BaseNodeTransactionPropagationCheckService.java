package io.coti.basenode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.services.interfaces.ITransactionPropagationCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.coti.basenode.services.BaseNodeServiceManager.*;

@Slf4j
@Service
public class BaseNodeTransactionPropagationCheckService implements ITransactionPropagationCheckService {

    protected Map<Hash, UnconfirmedReceivedTransactionHashData> unconfirmedReceivedTransactionHashesMap;
    protected final LockData transactionHashLockData = new LockData();

    @Override
    public void init() {
        log.info("{} is up", this.getClass().getSimpleName());
    }

    protected boolean isTransactionHashDSPConfirmed(Hash transactionHash) {
        TransactionData transactionData = transactions.getByHash(transactionHash);
        if (transactionData != null) {
            return nodeTransactionHelper.isDspConfirmed(transactionData);
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
        removeConfirmedReceivedTransactions(confirmedReceiptTransactions);
    }

    @Override
    public void removeTransactionHashFromUnconfirmed(Hash transactionHash) {
        if (unconfirmedReceivedTransactionHashesMap != null && unconfirmedReceivedTransactionHashesMap.containsKey(transactionHash)) {
            removeConfirmedReceiptTransaction(transactionHash);
        }
    }

    @Override
    public void addUnconfirmedTransactionDSPVote(TransactionDspVote transactionDspVote) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addPropagatedUnconfirmedTransaction(Hash hash) {
        throw new UnsupportedOperationException();
    }
}