package io.coti.cotinode.services;

import io.coti.cotinode.model.PreBalance;
import io.coti.cotinode.services.interfaces.ISourceSelector;
import io.coti.cotinode.model.Transaction;
import io.coti.cotinode.services.interfaces.ICluster;
import io.coti.cotinode.storage.Interfaces.IPersistenceProvider;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class Cluster implements ICluster {
    private IPersistenceProvider persistenceProvider;
    private ConcurrentHashMap<byte[], Transaction> hashToUnconfirmedTransactionsMapping;
    private ConcurrentHashMap<Integer, List<Transaction>> trustScoreToSourceListMapping;
    private ConcurrentHashMap<byte[], PreBalance> hashToPreBalanceMapping;

    @Override
    public void initCluster(List<Transaction> unconfirmedTransactions){
        hashToUnconfirmedTransactionsMapping = new ConcurrentHashMap<>();
        trustScoreToSourceListMapping = new ConcurrentHashMap<>();
        setUnconfirmedTransactions(unconfirmedTransactions);
        setTrustScoreToSourceListMapping(unconfirmedTransactions);
    }


    private void setUnconfirmedTransactions(List<Transaction> unconfirmedTransactions) {
        this.hashToUnconfirmedTransactionsMapping.
                putAll(unconfirmedTransactions.stream().
                        collect(Collectors.
                                toMap(Transaction::getHash, Function.identity())));
    }

    private void setTrustScoreToSourceListMapping(List<Transaction> unconfirmedTransactions) {
        this.trustScoreToSourceListMapping = new ConcurrentHashMap<>();
        for (Transaction transaction: unconfirmedTransactions) {
            if (transaction.isSource()){
                if (!this.trustScoreToSourceListMapping.containsKey(transaction.getSenderTrustScore())) {
                    this.trustScoreToSourceListMapping.put(transaction.getSenderTrustScore(), new Vector<Transaction>());
                }
                this.trustScoreToSourceListMapping.get(transaction.getSenderTrustScore()).add(transaction);
            }
        }
    }

    @Override
    public ConcurrentHashMap<byte[], Transaction> getUnconfirmedTransactions() {
        return hashToUnconfirmedTransactionsMapping;
    }

    @Override
    public void addUnconfirmedTransaction(Transaction transaction, byte[] hash) {
        hashToUnconfirmedTransactionsMapping.put(hash, transaction);
    }

    @Override
    public Transaction getTransaction(byte[] hash) {
        if(hashToUnconfirmedTransactionsMapping.containsKey(hash)){
            return hashToUnconfirmedTransactionsMapping.get(hash);
        }
        else{
            return persistenceProvider.getTransaction(hash);
            // Replace with a service that  TODO call the Service that will
        }
    }

    @Override
    public void deleteTransaction(byte[] hash) {
        if(hashToUnconfirmedTransactionsMapping.containsKey(hash)){
            hashToUnconfirmedTransactionsMapping.remove(hash);
        }

        persistenceProvider.deleteTransaction(hash);

    }

    public List<Transaction> getAllSourceTransactions() {
        return hashToUnconfirmedTransactionsMapping.values().stream().
                filter(Transaction::isSource).collect(Collectors.toList());
    }

    @Override
    public void addNewTransaction(Transaction transaction) {
        transaction.setProcessStartTime(new Date());

        // TODO: Validate the transaction, including balance && preBalance

        // TODO: Get The transaction trust score from trust score node.

        // Selection of sources
        ISourceSelector sourceSelector = new SourceSelector();
        List<Transaction> selectedSourcesForAttachment = sourceSelector.selectSourcesForAttachment( trustScoreToSourceListMapping,
                transaction.getSenderTrustScore(),
                transaction.getCreateTime(),
                5, // TODO: get value from config file and/or dynamic
                10); // TODO:  get value from config file and/or dynamic

        // TODO: Validate the sources.

        // POW
        transaction.setPowStartTime(new Date());
        // TODO : POW
        transaction.setPowEndTime(new Date());

        // Attache sources
        if (trustScoreToSourceListMapping.size() > 1) {
            if (selectedSourcesForAttachment.size() == 0) {
                // TODO: wait
            }

            for (Transaction sourceTransaction : selectedSourcesForAttachment) {
                attachToSource(transaction, sourceTransaction);
            }
        }

        // Update the total trust score of the parents
        //transaction.setChildrenTransactions(new Vector<byte[]>());
        updateParentsTotalSumScore(transaction, 0, transaction.getTrustChainTransactionHashes());

        transaction.setProcessEndTime(new Date());
    }

    @Override
    public void updateParentsTotalSumScore(Transaction transaction, int sonsTotalTrustScore, List<byte[]> trustChainTransactionHashes) {
        if (transaction != null && !transaction.isTransactionConsensus()) {
            if (transaction.getTotalTrustScore() <  sonsTotalTrustScore + transaction.getSenderTrustScore()) {
                transaction.setTotalTrustScore(sonsTotalTrustScore + transaction.getSenderTrustScore());
                transaction.setTrustChainTransactionHashes(trustChainTransactionHashes);
                if (transaction.getTotalTrustScore() >= 300 ) {// TODO : set the number as consant
                    if (transaction.isDspConsensus()) {
                        transaction.setTransactionConsensus(true);
                        hashToUnconfirmedTransactionsMapping.remove(transaction.getKey());
                    }
                }
            }
            List<byte[]> parentTrustChainTransactionHashes = (Vector<byte[]>)transaction.getTrustChainTransactionHashes();
            parentTrustChainTransactionHashes.add(transaction.getHash());
            updateParentsTotalSumScore(transaction.getLeftParent(),
                    transaction.getTotalTrustScore(),
                    parentTrustChainTransactionHashes);

            updateParentsTotalSumScore(transaction.getRightParent(),
                    transaction.getTotalTrustScore(),
                    parentTrustChainTransactionHashes);
        }
    }

    @Override
    public void attachToSource(Transaction newTransaction, Transaction source) {
        if(hashToUnconfirmedTransactionsMapping.get(source.getKey()) == null) {
            System.out.println("Cannot find source:" + source);
            throw new RuntimeException("Cannot find source:" + source);
        }
        newTransaction.attachToSource(source);
        newTransaction.setAttachmentTime(new Date());
    }


}

