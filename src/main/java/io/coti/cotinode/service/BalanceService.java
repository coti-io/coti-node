package io.coti.cotinode.service;

import io.coti.cotinode.data.BaseTransactionData;
import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.ConfirmationData;
import io.coti.cotinode.database.RocksDBConnector;
import io.coti.cotinode.model.ConfirmedTransactions;
import io.coti.cotinode.model.UnconfirmedTransactions;
import io.coti.cotinode.service.interfaces.IBalanceService;
import io.coti.cotinode.service.interfaces.ICluster;
import io.coti.cotinode.service.interfaces.IQueueService;
import lombok.extern.slf4j.Slf4j;
import org.rocksdb.RocksIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
public class BalanceService implements IBalanceService {

    @Autowired
    private IQueueService queueService;

    @Autowired
    private RocksDBConnector databaseConnector;

    @Autowired
    private ConfirmedTransactions confirmedTransactions;

    @Autowired
    private UnconfirmedTransactions unconfirmedTransactions;

    @Autowired
    private ICluster clusterService;

    private Map<Hash, Double> balanceMap;
    private Map<Hash, Double> preBalanceMap;


    private List<Map<Hash, Double>> unconfirmedTransactionList;
    private List<Map<Hash, Double>> confirmedTransactionList;

    private boolean startScheduledTask;

    @PostConstruct
    private void init() {
        try {
            balanceMap = new ConcurrentHashMap<>();
            preBalanceMap = new ConcurrentHashMap<>();
            unconfirmedTransactionList = new LinkedList<>();
            confirmedTransactionList = new LinkedList<>();

            loadBalanceFromSnapshot();

            List<ConfirmationData> unconfirmedTransactionsToDelete = new LinkedList<>();

            // UnconfirmedTransactionFromDB init
            readUnconfirmedTransactionsFromDB(unconfirmedTransactionsToDelete);

            // ConfirmedTransactionFromDB init
            readConfirmedTransactionsFromDB();

            //move items from unnconfirmed to confirmed db table
            removeFromUnconfirmedAndFillConfirmedInDB(unconfirmedTransactionsToDelete);


            // call cluster service with the unconfirmedTransactionList

            //move balances from unconfirmed/confirmed Transaction Map To Balance Maps
            insertFromTempDBlistToInMemMap(confirmedTransactionList, balanceMap); // calc
            insertFromTempDBlistToInMemMap(unconfirmedTransactionList, preBalanceMap); // calc
            confirmedTransactionList.clear();
            unconfirmedTransactionList.clear();


            startScheduledTask = true;

        } catch (Exception ex) {
            log.error("Errors on initiation ", ex);
        }
    }

    /**
     * The task will be executed a first time after the initialDelay (because of the init() ) value – and it will
     * continue to be executed according to the fixedDelay
     */
    @Scheduled(fixedDelayString = "${balance.scheduled.delay}", initialDelayString = "${balance.scheduled.initialdelay}")
    private void syncBalanceScheduled() {
        if(startScheduledTask) {
            updateDbFromQueue();
        }
    }

    private void updateDbFromQueue() {
        ConcurrentLinkedQueue<Hash> updateBalanceQueue = queueService.getUpdateBalanceQueue();
        log.info("Balance queue size is {} about to iterate it", updateBalanceQueue.size());
        while (!updateBalanceQueue.isEmpty()) {
            Hash addressHash = updateBalanceQueue.poll();
            ConfirmationData confirmationData = unconfirmedTransactions.getByHash(addressHash);
            log.info("confirmationData hash;{}",addressHash );
            //dspc = 1
            if (confirmationData.isDoubleSpendPreventionConsensus()) {
                ConfirmationData confirmedTransactionData = new ConfirmationData(addressHash);
                confirmedTransactionData.setAddressHashToValueTransferredMapping(confirmationData.getAddressHashToValueTransferredMapping());
                confirmedTransactions.put(confirmedTransactionData);
                unconfirmedTransactions.delete(addressHash);
                for (Map.Entry<Hash, Double> mapEntry : confirmationData.getAddressHashToValueTransferredMapping().entrySet()) {
                    balanceMap.put(mapEntry.getKey(), mapEntry.getValue());
                    log.info("The address {} with the value {} was added to balance map and was removed from preBalanceMap", mapEntry.getKey(), mapEntry.getValue());
                }
            } else { //dspc =0
                confirmationData.setTrustChainConsensus(true);
                unconfirmedTransactions.put(confirmationData);
                log.info("The transaction {} was added to unconfirmedTransactions in the db and tcc was updated to true", confirmationData.getKey());

            }
        }
    }

    private void removeFromUnconfirmedAndFillConfirmedInDB(List<ConfirmationData> unconfirmedTransactionsToDelete) {
        for (ConfirmationData confirmationData : unconfirmedTransactionsToDelete) {
            confirmedTransactions.put(confirmationData);
            unconfirmedTransactions.delete(confirmationData.getHash());
            log.info("The transaction {} was removed from unconfirmedTransactions in the db", confirmationData.getKey());

        }
        unconfirmedTransactionsToDelete.clear();
    }

    private void insertFromTempDBlistToInMemMap(List<Map<Hash, Double>> addressToBalanceMapFromDB , Map<Hash, Double> addressToBalanceMapInMem ) {
        for (Map<Hash, Double> addressToBalanceMap : addressToBalanceMapFromDB) {
            for (Map.Entry<Hash, Double> entry : addressToBalanceMap.entrySet()) {
                double balance = entry.getValue();
                Hash key = entry.getKey();
                if(addressToBalanceMapInMem.containsKey(key)) {
                    addressToBalanceMapInMem.put(key, balance + addressToBalanceMapInMem.get(key));
                }
                else {
                    addressToBalanceMapInMem.put(key, balance);
                }
            }
        }
    }

    private void readConfirmedTransactionsFromDB() {
        RocksIterator confirmedDBiterator = databaseConnector.getIterator(ConfirmedTransactions.class.getName());
        confirmedDBiterator.seekToFirst();
        while (confirmedDBiterator.isValid()) {
            ConfirmationData confirmedTransactionData = (ConfirmationData) SerializationUtils
                    .deserialize(confirmedDBiterator.value());
            confirmedTransactionList.add(confirmedTransactionData.getAddressHashToValueTransferredMapping());

            confirmedDBiterator.next();

        }
    }

    private void readUnconfirmedTransactionsFromDB(List<ConfirmationData> unconfirmedTransactionsToDelete) {

        RocksIterator unconfirmedDBiterator = databaseConnector.getIterator(UnconfirmedTransactions.class.getName());
        unconfirmedDBiterator.seekToFirst();
        while (unconfirmedDBiterator.isValid()) {
            ConfirmationData confirmationData = (ConfirmationData) SerializationUtils
                    .deserialize(unconfirmedDBiterator.value());
            if (confirmationData.isTrustChainConsensus()) { //tcc =1
                if (confirmationData.isDoubleSpendPreventionConsensus()) { // tcc = 1 + dspc = 1
                    confirmedTransactionList.add(confirmationData.getAddressHashToValueTransferredMapping());
                    unconfirmedTransactionsToDelete.add(confirmationData);
                } else { // tcc = 1 + dspc = 0
                    unconfirmedTransactionList.add(confirmationData.getAddressHashToValueTransferredMapping());
                }
            } else { //tcc = 0      dspc 0/1
                unconfirmedTransactionList.add(confirmationData.getAddressHashToValueTransferredMapping());
                // TCC QUEUE -> QUEUE Service
                queueService.addToTccQueue(confirmationData.getHash());
            }
            unconfirmedDBiterator.next();
        }
    }


    public synchronized boolean checkBalancesAndAddToPreBalance(List<BaseTransactionData> baseTransactionDatas) {
        try {
            for (BaseTransactionData baseTransactionData : baseTransactionDatas) {
                //checkBalance
                double amount = baseTransactionData.getAmount();
                Hash addressHash = baseTransactionData.getAddressHash();
                if ((balanceMap.containsKey(addressHash) && amount + balanceMap.get(addressHash) < 0)
                        || (!balanceMap.containsKey(addressHash) && amount < 0)) {
                    log.error("Error in Balance check. Address {}  amount {} current Balance {} ", addressHash,
                            amount, preBalanceMap.get(addressHash));
                    return false;
                }


                if(preBalanceMap.containsKey(addressHash)){
                    if(amount + preBalanceMap.get(addressHash) < 0){
                        log.error("Error in preBalance check. Address {}  amount {} current preBalance {} ", addressHash,
                                amount, preBalanceMap.get(addressHash));
                        return false;
                    }
                    else{
                        preBalanceMap.put(addressHash, amount + preBalanceMap.get(addressHash));
                    }
                }
                else{
                    if(amount  < 0){
                        log.error("Error in preBalance check. Address {}  amount {} current preBalance {} ", addressHash,
                                amount, preBalanceMap.get(addressHash));
                        return false;
                    }
                    preBalanceMap.put(addressHash, amount );
                }

            }
        } catch (Exception ex) {
            log.error("Exception on checking balances and adding to preBalance {}", ex);
        }
        return true;
    }



    public void insertIntoUnconfirmedDBandAddToTccQeueue(ConfirmationData confirmationData) {
        // put it in unconfirmedTransaction table
        unconfirmedTransactions.put(confirmationData);
        queueService.addToTccQueue(confirmationData.getHash());
    }

    private void loadBalanceFromSnapshot() {
        String snapshotFileLocation = "./Snapshot.csv";
        File snapshotFile = new File(snapshotFileLocation);

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(snapshotFile));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                String[] addressDetails = line.split(",");
                if (addressDetails.length != 2) {
                    throw new Exception("Bad csv file format");
                }
                Hash addressHash = new Hash(addressDetails[0]);
                Double addressAmount = Double.parseDouble(addressDetails[1]);
                log.info("The hash {} was loaded from the snapshot with amount {}", addressHash, addressAmount);

                if (balanceMap.containsKey(addressHash)) {
                    // throw new Exception(String.format("Double address found in CSV file: %s", addressHash));
                    log.error("The address {} was already found in the snapshot", addressHash);
                }
                balanceMap.put(addressHash, addressAmount);
                log.info("Loading from snapshot into inMem balance+preBalance address {} and amount {}",
                        addressHash,addressAmount);

            }
            // copy the balance to preBalance
            preBalanceMap.putAll(balanceMap);
        } catch (Exception e) {
            log.error("Errors on snapshot loading: {}", e);
        }
    }

    public Map<Hash, Double> getBalanceMap() {
        return balanceMap;
    }
    public Map<Hash, Double> getPreBalanceMap() {
        return preBalanceMap;
    }

}
