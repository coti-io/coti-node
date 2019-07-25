package io.coti.historynode.services;

import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.ClusterStampData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.AddHistoryEntitiesResponse;
import io.coti.basenode.services.BaseNodeClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    @Autowired
    private ClusterStampCrypto clusterStampCrypto;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private AddressService addressService;

    public void newClusterStamp(ClusterStampData clusterStampData) {
        if (clusterStampCrypto.verifySignature(clusterStampData)) {
//            clusterStamps.put(clusterStampData); //TODO: For initial compilation prior to merge
        }

        // Copy Addresses from local DB to Storage

        // Delete current Addresses from local DB //TODO: consider synchronization issues

        //


    }

    //    @Override
    protected void terminateClusterStampByNodeType() {
        // TODO implement
    }

    //    @Override
    public Set<Hash> getUnreachedDspcHashTransactions() {
        // TODO consider implementation change.
        return null;
    }

//    @Override //TODO: For initial compilation prior to merge
//    public void handleClusterStampConsensusResult(ClusterStampConsensusResult clusterStampConsensusResult) {
//        if (clusterStampConsensusResultCrypto.verifySignature(clusterStampConsensusResult)) {
//            ClusterStampData lastClusterStamp = clusterStamps.getByHash(clusterStampConsensusResult.getClusterStampHash());
//            transactionService.deleteLocalUnconfirmedTransactions();
//            storeData(lastClusterStamp);
//        }
//    }

    private void storeData(ClusterStampData lastClusterStamp) {
        List<TransactionData> unconfirmedTransactionsFromClusterStamp = new ArrayList<>();
        List<Hash> unconfirmedTransactionHashesFromClusterStamp =
                unconfirmedTransactionsFromClusterStamp.stream().map(transactionData -> transactionData.getHash()).collect(Collectors.toList());
        Set<AddressData> unconfirmedTransactionsAddresses = new HashSet<>();
//TODO: For initial compilation prior to merge
//        lastClusterStamp.getUnconfirmedTransactions().values().forEach(transaction -> {
//            addressTransactionsService.saveToAddressTransactionsHistories(transaction);
//            unconfirmedTransactions.add(transaction);
//            unconfirmedTransactionsAddresses.add(new AddressData(transaction.getSenderHash()));
//        });

        // TODO: later construct consensus based on algorithm

        //TODO 7/4/2019 tomer:
        // Get local unconfirmed transactions hashes
        transactionService.updateUnconfirmedTransactionsNotFromClusterStamp(unconfirmedTransactionHashesFromClusterStamp);

        // Replace / update matching transactions in DB with the entries from the cluster-stamp
        unconfirmedTransactionsFromClusterStamp.forEach(transactionData -> {
            if (transactions.getByHash(transactionData.getHash()) == null) {
                transactionService.addToHistoryTransactionIndexes(transactionData);
            }
            transactions.put(transactionData);
        });

        // Store in Elastic-search all of the confirmed transactions, remove from local storage those that were stored in ES successfully
        List<TransactionData> confirmedTransactionsToStore = new ArrayList<>();
        transactions.forEach(transactionData -> {
            if (transactionData.isTrustChainConsensus() && transactionData.getDspConsensusResult().isDspConsensus()) {
                confirmedTransactionsToStore.add(transactionData);
            }
        });

        //TODO 7/7/2019 tomer: Consider sending in groups of size..
        ResponseEntity<AddHistoryEntitiesResponse> storeEntitiesToStorageResponse = transactionService.storeEntities(confirmedTransactionsToStore);
        if (storeEntitiesToStorageResponse.getStatusCode().equals(HttpStatus.OK)) {
            Map<Hash, Boolean> entitiesSentToStorage = storeEntitiesToStorageResponse.getBody().getHashesToStoreResult();
            entitiesSentToStorage.entrySet().forEach(pair -> {
                if (pair.getValue().equals(Boolean.TRUE)) {
                    transactions.deleteByHash(pair.getKey());
                }
            });
        }

        //TODO 7/7/2019 tomer: Add handling of response
        //historyAddressService.storeEntities(new ArrayList<>(unconfirmedTransactionsAddresses));
        addressService.handleClusterStampAddressesStorage();
    }


//TODO: For initial compilation prior to merge
//    @Override
//    public void prepareForClusterStamp(ClusterStampPreparationData clusterStampPreparationData) {
//        // TODO: Implement this
//    }
//
//    @Override
//    public void getReadyForClusterStamp(ClusterStampStateData nodeReadyForClusterStampData) {
//        // TODO: Implement this
//    }
}