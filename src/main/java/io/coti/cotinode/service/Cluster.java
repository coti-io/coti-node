package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.service.interfaces.ISourceSelector;

import io.coti.cotinode.service.interfaces.ICluster;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@Data
public class Cluster implements ICluster {

    //region init process
    //private IPersistenceProvider persistenceProvider; // TODO: replace with TransactionService

    @Autowired
    private ISourceSelector sourceSelector;
    private ConcurrentHashMap<Hash, TransactionData> hashToAllClusterTransactionsMapping;
    private ConcurrentHashMap<Hash, TransactionData> hashToUnTccConfirmationTransactionsMapping;
    private ConcurrentHashMap<Integer, List<TransactionData>> trustScoreToSourceListMapping;

    @Override
    public void initCluster(List<TransactionData> allClusterTransactions){
        hashToAllClusterTransactionsMapping = new ConcurrentHashMap<>();;
        hashToUnTccConfirmationTransactionsMapping = new ConcurrentHashMap<>();
        trustScoreToSourceListMapping = new ConcurrentHashMap<>();

        setAllClusterTransactionsMap(allClusterTransactions);
        setUnTccConfirmedTransactions(allClusterTransactions);
        setTrustScoreToSourceListMapping(hashToUnTccConfirmationTransactionsMapping);
    }

    private void setAllClusterTransactionsMap(List<TransactionData> allTransactions) {
        this.hashToUnTccConfirmationTransactionsMapping.
                putAll(allTransactions.stream().
                        collect(Collectors.
                                toMap(TransactionData::getHash, Function.identity())));
    }

    private void setUnTccConfirmedTransactions(List<TransactionData> allTransactions) {
        this.hashToUnTccConfirmationTransactionsMapping.
                putAll(allTransactions.stream().
                        filter(TransactionData::isConfirm).
                        collect(Collectors.
                                toMap(TransactionData::getHash, Function.identity())));
    }

    private void setTrustScoreToSourceListMapping(ConcurrentHashMap<Hash, TransactionData> hashToUnconfirmedTransactionsMapping) {
        this.trustScoreToSourceListMapping = new ConcurrentHashMap<>();
        for (int i=1; i <= 100; i++) {
            trustScoreToSourceListMapping.put(i, new Vector<TransactionData>());
        }

        for ( TransactionData transaction: hashToUnconfirmedTransactionsMapping.values()) {
            if (transaction.isSource() && transaction.getSenderTrustScore() >=1 && transaction.getSenderTrustScore() <=100 ){
                this.trustScoreToSourceListMapping.get(transaction.getSenderTrustScore()).add(transaction);
            }
        }
    }
    //endregion

    //region Description
    @Override
    public void addToHashToAllClusterTransactionsMap(TransactionData transaction) {
        hashToAllClusterTransactionsMapping.put(transaction.getHash(), transaction);
        // TODO use the TransactionService
    }

    @Override
    public void addToUnTccConfirmedTransactionMap(TransactionData transaction) {
        hashToUnTccConfirmationTransactionsMapping.put(transaction.getHash(), transaction);
        // TODO use the TransactionService
    }

    @Override
    public void addToTrustScoreToSourceListMap(TransactionData transaction) {

        if (transaction.isSource() && transaction.getSenderTrustScore() >=1 && transaction.getSenderTrustScore() <=100){
            this.trustScoreToSourceListMapping.get(transaction.getSenderTrustScore()).add(transaction);
        }
        // TODO use the TransactionService
    }

    @Override
    public void deleteTransactionFromHashToUnTccConfirmedTransactionsMapping(Hash hash) {
        TransactionData transaction = null;
        if(hashToUnTccConfirmationTransactionsMapping.containsKey(hash)){
            transaction = hashToUnTccConfirmationTransactionsMapping.get(hash);
            hashToUnTccConfirmationTransactionsMapping.remove(hash);
        }

        //persistenceProvider.deleteTransaction(hash);
        // TODO: replace with TransactionService

        deleteTrustScoreToSourceListMapping(hash, transaction);
    }

    @Override
    public void deleteTrustScoreToSourceListMapping(Hash hash, TransactionData transaction ) {
        if (trustScoreToSourceListMapping.containsKey(transaction.getSenderTrustScore())) {
            trustScoreToSourceListMapping.get(transaction.getSenderTrustScore()).remove(transaction);
        }
        else {
            for (List<TransactionData> transactionList : trustScoreToSourceListMapping.values()) {
                if (transactionList.contains(transaction)) {
                    transactionList.remove(transaction);
                }
            }
        }

        //persistenceProvider.deleteTransaction(hash);
        // TODO: replace with TransactionService
    }

    @Override
    public List<TransactionData> getAllSourceTransactions() {
        return hashToUnTccConfirmationTransactionsMapping.values().stream().
                filter(TransactionData::isSource).collect(Collectors.toList());
    }
    //endregion

    //region Adding new transaction Process
    @Override
    public boolean addNewTransaction(TransactionData transaction, boolean isFromPropagation) {
        transaction.setProcessStartTime(new Date());

        // TODO: Validate the transaction, including balance && preBalance. Maybe it will be out of the Cluster class

        // TODO: Get The transaction trust score from trust score node.

        List<TransactionData> selectedSourcesForAttachment = null;
        ConcurrentHashMap<Integer, List<TransactionData>> localThreadTustScoreToSourceListMapping =
                new ConcurrentHashMap<>(trustScoreToSourceListMapping);
        if (localThreadTustScoreToSourceListMapping.size() > 1) {

            // Selection of sources
            selectedSourcesForAttachment = sourceSelector.selectSourcesForAttachment(localThreadTustScoreToSourceListMapping,
                    transaction.getSenderTrustScore(),
                    transaction.getAttachmentTime(),
                    5, // TODO: get value from config file and/or dynamic
                    10); // TODO:  get value from config file and/or dynamic
        }

        // TODO: Validate the sources.

        // POW
        if (isFromPropagation) {
            transaction.setPowStartTime(new Date());
            // TODO : POW
            transaction.setPowEndTime(new Date());
        }

        // Attache sources
        if (localThreadTustScoreToSourceListMapping.size() > 1) {
            if (selectedSourcesForAttachment.size() == 0) {
                // TODO: wait
            }

            for (TransactionData sourceTransaction : selectedSourcesForAttachment) {
                attachToSource(transaction, sourceTransaction);
            }
        }

        // updating transaction collections with the new transaction
        addNewTransactionToAllCollections (transaction);

        // Update the total trust score of the parents
        updateParentsTotalSumScore(transaction, 0, transaction.getTrustChainTransactionHashes());

        transaction.setProcessEndTime(new Date());

        return true;
    }

    private void addNewTransactionToAllCollections (TransactionData transaction)
    {
        // add to allClusterTransactions map
        addToHashToAllClusterTransactionsMap(transaction);

        // add to unTccConfirmedTransaction map
        addToUnTccConfirmedTransactionMap(transaction);

        //  add to TrustScoreToSourceList map
        addToTrustScoreToSourceListMap(transaction);
    }

    @Override
    public void updateParentsTotalSumScore(TransactionData transaction, int sonsTotalTrustScore, List<Hash> trustChainTransactionHashes) {
        if (transaction != null && !transaction.isTransactionConsensus()) {
            if (transaction.getTotalTrustScore() <  sonsTotalTrustScore + transaction.getSenderTrustScore()) {
                transaction.setTotalTrustScore(sonsTotalTrustScore + transaction.getSenderTrustScore());
                transaction.setTrustChainTransactionHashes(trustChainTransactionHashes);
                if (transaction.getTotalTrustScore() >= 300 ) {// TODO : set the number as consant
                    transaction.setTransactionConsensus(true);
                    hashToUnTccConfirmationTransactionsMapping.remove(transaction.getKey());
                }
            }
            List<Hash> parentTrustChainTransactionHashes = new Vector<Hash>(transaction.getTrustChainTransactionHashes());
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
    public void attachToSource(TransactionData newTransaction, TransactionData source) {
        if(hashToAllClusterTransactionsMapping.get(source.getKey()) == null) {
            log.error("Cannot find source:" + source);
            //throw new RuntimeException("Cannot find source:" + source);
        }
        newTransaction.attachToSource(source);
        newTransaction.setAttachmentTime(new Date());
        deleteTrustScoreToSourceListMapping(source.getHash(), source);
    }
    //endregion
}

