package io.coti.historynode.services;

import io.coti.basenode.crypto.ClusterStampCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.services.BaseNodeClusterStampService;
import io.coti.historynode.services.interfaces.IAddressTransactionsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    @Value("#{'${receiving.server.addresses}'.split(',')}")
    private List<String> receivingServerAddresses;
    @Autowired
    private ClusterStampCrypto clusterStampCrypto;
    @Autowired
    private HistoryTransactionService historyTransactionService;
    @Autowired
    private HistoryAddressService historyAddressService;
    @Autowired
    private IAddressTransactionsService addressTransactionsService;

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
        List<TransactionData> unconfirmedTransactions = new ArrayList<>();
        Set<AddressData> unconfirmedTransactionsAddresses = new HashSet<>();
//TODO: For initial compilation prior to merge
//        lastClusterStamp.getUnconfirmedTransactions().values().forEach(transaction -> {
//            addressTransactionsService.saveToAddressTransactionsHistories(transaction);
//            unconfirmedTransactions.add(transaction);
//            unconfirmedTransactionsAddresses.add(new AddressData(transaction.getSenderHash()));
//        });

        // TODO: later construct consensus based on algorithm
        historyTransactionService.storeEntities(unconfirmedTransactions);
        historyAddressService.storeEntities(new ArrayList<>(unconfirmedTransactionsAddresses));
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