package io.coti.cotinode.services;

import io.coti.cotinode.services.interfaces.ISourceSelector;
import io.coti.cotinode.model.Interfaces.ITransaction;
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
    private ConcurrentHashMap<byte[], ITransaction> hashToUnconfirmedTransactionsMapping;
    private ConcurrentHashMap<Integer, List<ITransaction>> trustScoreToSourceListMapping;

    @Override
    public void initCluster(List<ITransaction> unconfirmedTransactions){
        hashToUnconfirmedTransactionsMapping = new ConcurrentHashMap<>();
        trustScoreToSourceListMapping = new ConcurrentHashMap<>();
        setUnconfirmedTransactions(unconfirmedTransactions);
        setTrustScoreToSourceListMapping(unconfirmedTransactions);
    }


    private void setUnconfirmedTransactions(List<ITransaction> unconfirmedTransactions) {
        this.hashToUnconfirmedTransactionsMapping.
                putAll(unconfirmedTransactions.stream().
                        collect(Collectors.
                                toMap(ITransaction::getHash, Function.identity())));
    }

    private void setTrustScoreToSourceListMapping(List<ITransaction> unconfirmedTransactions) {
        this.trustScoreToSourceListMapping = new ConcurrentHashMap<Integer, List<ITransaction>>();
        for (ITransaction transaction: unconfirmedTransactions) {
            if (transaction.isSource()){
                if (!this.trustScoreToSourceListMapping.containsKey(transaction.getSenderTrustScore())) {
                    this.trustScoreToSourceListMapping.put(transaction.getSenderTrustScore(), new Vector<ITransaction>());
                }
                this.trustScoreToSourceListMapping.get(transaction.getSenderTrustScore()).add(transaction);
            }
        }
    }

    @Override
    public ConcurrentHashMap<byte[], ITransaction> getUnconfirmedTransactions() {
        return hashToUnconfirmedTransactionsMapping;
    }

    @Override
    public void addUnconfirmedTransaction(ITransaction transaction, byte[] hash) {
        hashToUnconfirmedTransactionsMapping.put(hash, transaction);
    }

    @Override
    public ITransaction getTransaction(byte[] hash) {
        if(hashToUnconfirmedTransactionsMapping.containsKey(hash)){
            return hashToUnconfirmedTransactionsMapping.get(hash);
        }
        else{
            return persistenceProvider.getTransaction(hash);
        }
    }

    @Override
    public void deleteTransaction(byte[] hash) {
        if(hashToUnconfirmedTransactionsMapping.containsKey(hash)){
            hashToUnconfirmedTransactionsMapping.remove(hash);
        }
        else{
            persistenceProvider.deleteTransaction(hash);
        }
    }

    public List<ITransaction> getAllSourceTransactions() {
        return hashToUnconfirmedTransactionsMapping.values().stream().
                filter(ITransaction::isSource).collect(Collectors.toList());
    }

    @Override
    public void addNewTransaction(ITransaction transaction) {
        transaction.setProcessStartTime(new Date());

        // TODO Validation

        // Selection of sources
        ISourceSelector sourceSelector = new SourceSelector();
        List<ITransaction> selectedSourcesForAttachment = sourceSelector.selectSourcesForAttachment( trustScoreToSourceListMapping,
                transaction.getSenderTrustScore(),
                transaction.getCreateDateTime(),
                5, // TODO: from config file and/or dynamic
                10); // TODO: from config file and/or dynamic

        // Update sources
        for (ITransaction sourceTransaction : selectedSourcesForAttachment) {
            attachToSource(transaction, sourceTransaction);
        }

        // Update the total trust score of the parents
        transaction.setChildrenTransactions(new Vector<byte[]>());
        updateParentsTotalSumScore(transaction, 0);

        // POW
        transaction.setPowStartTime(new Date());
        // TODO : POW
        transaction.setPowEndTime(new Date());


        for (ITransaction sourceTransaction : selectedSourcesForAttachment) {

        }

        transaction.setProcessEndTime(new Date());
    }

    @Override
    public void updateParentsTotalSumScore(ITransaction transaction, int sonsTotalTrustScore) {
        if (transaction != null && !transaction.isThresholdAchieved()) {
            if (transaction.getTotalTrustScore() <  sonsTotalTrustScore + transaction.getSenderTrustScore()) {
                transaction.setTotalTrustScore(sonsTotalTrustScore + transaction.getSenderTrustScore());
                if (transaction.getTotalTrustScore() >= 300 ) {// TODO : set the number as consant
                    transaction.setThresholdAchieved(true);
                    hashToUnconfirmedTransactionsMapping.remove(transaction.getKey());
                }
            }
            updateParentsTotalSumScore(transaction.getLeftParent(), transaction.getTotalTrustScore());
            updateParentsTotalSumScore(transaction.getRightParent(), transaction.getTotalTrustScore());
        }
    }

    @Override
    public void attachToSource(ITransaction newTransaction, ITransaction source) {
        if(hashToUnconfirmedTransactionsMapping.get(source.getKey()) == null) {
            System.out.println("Cannot find source:" + source);
            throw new RuntimeException("Cannot find source:" + source);
        }
        newTransaction.attachToSource(source);
        newTransaction.setAttachmentTime(new Date());
    }


}

