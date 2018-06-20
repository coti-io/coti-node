package io.coti.cotinode.service;

import io.coti.cotinode.AppConfig;
import io.coti.cotinode.data.*;
import io.coti.cotinode.data.interfaces.IEntity;
import io.coti.cotinode.database.Interfaces.IDatabaseConnector;
import io.coti.cotinode.model.UnconfirmedTransaction;
import io.coti.cotinode.service.interfaces.IBalanceService;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Service
public class BalanceService implements IBalanceService {

    @Autowired
    private AppConfig appConfig;

    @Autowired
    private IQueueService queueService;

    @Autowired
    private IDatabaseConnector databaseConnector;

    private Map<Hash, Double> balanceMap;
    private Map<Hash, Double> preBalanceMap;

    private List<Map<Hash, Double>> unconfirmedTransactionMap;
    private List<Map<Hash, Double>> confirmedTransactionMap;

    @PostConstruct
    private void init() {

        loadBalanceFromSnapshot();

        balanceMap = new ConcurrentHashMap<>();
        preBalanceMap = new ConcurrentHashMap<>();
        unconfirmedTransactionMap = new LinkedList<>();
        confirmedTransactionMap = new LinkedList<>();
        List<UnconfirmedTransactionData> unconfirmedTransactionsToDelete = new LinkedList<>();

        // UnconfirmedTransactionFromDB init
        unconfirmedTransactionFromDBinit(unconfirmedTransactionsToDelete);

        // ConfirmedTransactionFromDB init
        confirmedTransactionFromDBinit();

        //move items from unnconfirmed to confirmed db table
        unconfirmedToConfirmedDB(unconfirmedTransactionsToDelete);

        //move balances from unconfirmed Transaction Map To Balance Maps
        unconfirmedTransactionMapToBalanceMaps();
    }

    private void unconfirmedToConfirmedDB(List<UnconfirmedTransactionData> unconfirmedTransactionsToDelete) {
        for(UnconfirmedTransactionData unconfirmedTransactionData: unconfirmedTransactionsToDelete){
            databaseConnector.put(appConfig.getConfirmedTransactionsColoumnFamilyName(),
                    unconfirmedTransactionData.getKey().getBytes(),unconfirmedTransactionData.getHash().getBytes());
            databaseConnector.delete(appConfig.getUnconfirmedTransactionsColoumnFamilyName(),unconfirmedTransactionData
                    .getKey().getBytes());
        }
    }

    private void unconfirmedTransactionMapToBalanceMaps() {
        for(Map<Hash, Double> addressToBalanceMap : unconfirmedTransactionMap) {
            for (Map.Entry<Hash, Double> entry : addressToBalanceMap.entrySet()) {
                double balance = entry.getValue();
                Hash key = entry.getKey();
                preBalanceMap.put(key,balance);
                if(balanceMap.containsKey(key)){
                    balanceMap.put(key,balanceMap.get(key) + balance);
                }
                else{
                    balanceMap.put(key,balance);

                }
            }

        }
    }

    private void confirmedTransactionFromDBinit() {
        RocksIterator confirmedDBiterator = databaseConnector.getIterator(appConfig.getUnconfirmedTransactionsColoumnFamilyName());
        confirmedDBiterator.seekToFirst();
        while (confirmedDBiterator.isValid()) {
            ConfirmedTransactionData confirmedTransactionData = (ConfirmedTransactionData) SerializationUtils
                    .deserialize(confirmedDBiterator.value());
            confirmedTransactionMap.add(confirmedTransactionData.getAddressHashToValueTransferredMapping());

            confirmedDBiterator.next();

        }
    }

    private void unconfirmedTransactionFromDBinit(List<UnconfirmedTransactionData> unconfirmedTransactionsToDelete) {
        RocksIterator unconfirmedDBiterator = databaseConnector.getIterator(appConfig.getUnconfirmedTransactionsColoumnFamilyName());
        unconfirmedDBiterator.seekToFirst();
        while (unconfirmedDBiterator.isValid()) {
            UnconfirmedTransactionData unconfirmedTransactionData = (UnconfirmedTransactionData) SerializationUtils
                    .deserialize(unconfirmedDBiterator.value());
            Hash transactionHashFromDB = (Hash) SerializationUtils.deserialize(unconfirmedDBiterator.key());
            if(unconfirmedTransactionData.isTrustChainConsensus()){ //tcc =1
                if(unconfirmedTransactionData.isDoubleSpendPreventionConsensus()){ // tcc = 1 + dspc = 1
                    confirmedTransactionMap.add(unconfirmedTransactionData.getAddressHashToValueTransferredMapping());
                    unconfirmedTransactionsToDelete.add(unconfirmedTransactionData);
                }
                else{ // tcc = 1 + dspc = 0
                    unconfirmedTransactionMap.add(unconfirmedTransactionData.getAddressHashToValueTransferredMapping());
                }
            }
            else{ //tcc = 0
                unconfirmedTransactionMap.add(unconfirmedTransactionData.getAddressHashToValueTransferredMapping());
                // TCC QUEUE -> QUEUE Service
                queueService.addToTccQueue(unconfirmedTransactionData.getHash());
            }
            unconfirmedDBiterator.next();
        }
    }

    /**
     * The task will be executed a first time after the initialDelay (because of the init() ) value – and it will
     * continue to be executed according to the fixedDelay
     */
    @Scheduled(fixedDelay = 5000, initialDelay = 1000)
    private void syncBalanceScheduled() {
        updateDbFromQueue();
    }

    private void updateDbFromQueue(){
        ConcurrentLinkedQueue<Hash> updateBalanceQueue = queueService.getUpdateBalanceQueue();
        while(!updateBalanceQueue.isEmpty()){
            Hash addressHash = updateBalanceQueue.poll();
            UnconfirmedTransactionData unfonUnconfirmedTransactionData = (UnconfirmedTransactionData)SerializationUtils
                    .deserialize( databaseConnector.getByKey(appConfig.getUnconfirmedTransactionsColoumnFamilyName(),addressHash.getBytes()));
            //dspc = 1
            if(unfonUnconfirmedTransactionData.isDoubleSpendPreventionConsensus()){
                ConfirmedTransactionData confirmedTransactionData = new ConfirmedTransactionData(addressHash);
                confirmedTransactionData.setAddressHashToValueTransferredMapping(unfonUnconfirmedTransactionData.getAddressHashToValueTransferredMapping());

                databaseConnector.put(appConfig.getConfirmedTransactionsColoumnFamilyName(),addressHash.getBytes(),
                        confirmedTransactionData.getHash().getBytes());
                databaseConnector.delete(appConfig.getUnconfirmedTransactionsColoumnFamilyName(),addressHash.getBytes());
                for(Map.Entry<Hash,Double> mapEntry : unfonUnconfirmedTransactionData.getAddressHashToValueTransferredMapping().entrySet()){
                    balanceMap.put(mapEntry.getKey(),mapEntry.getValue());
                    log.info("The address {} with the value {} was added to balance map",mapEntry.getKey(), mapEntry.getValue());
                    preBalanceMap.remove(mapEntry.getKey());
                }
            }
            else{ //dspc =0
                unfonUnconfirmedTransactionData.setTrustChainConsensus(true);
                databaseConnector.put(appConfig.getUnconfirmedTransactionsColoumnFamilyName(),addressHash.getBytes(),
                        unfonUnconfirmedTransactionData.getHash().getBytes());

            }
        }
    }

    public boolean inMemorySync(List<Map.Entry<Hash, Double>> pairList){
        for(Map.Entry<Hash, Double> mapEntry : pairList){
            //checkBalance
            double amount = mapEntry.getValue();
            Hash addressHash = mapEntry.getKey();
            if(balanceMap.containsKey(addressHash) && amount + balanceMap.get(addressHash) < 0){
                log.error("The address {} with the amount {} is exceeds it's current balance {} ",addressHash.toString(),
                        amount,balanceMap.get(addressHash));
                return false;
            }
            //checkPreBalance
            if(preBalanceMap.containsKey(addressHash) && amount + preBalanceMap.get(addressHash)< 0){
                log.error("The address {} with the amount {} is exceeds it's current preBalance {} ",addressHash.toString(),
                        amount,preBalanceMap.get(addressHash));
                return false;
            }
            else{//update preBalance
                preBalanceMap.put(addressHash,amount + preBalanceMap.get(addressHash));
            }

        }
        return true;
    }

    public void dbSync(UnconfirmedTransactionData unconfirmedTransactionData){
        for(Map.Entry<Hash, Double> mapEntry : unconfirmedTransactionData.getAddressHashToValueTransferredMapping().entrySet()){
            queueService.addToTccQueue(mapEntry.getKey());
        }
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
                Hash addressHash = new Hash(addressDetails[0].getBytes());
                Double addressAmount = Double.parseDouble(addressDetails[1]);
                log.info("The hash {} was loaded from the snapshot with amount", addressHash, addressAmount);

                if (balanceMap.containsKey(addressHash)) {
                    // throw new Exception(String.format("Double address found in CSV file: %s", addressHash));
                    log.error("The address {} was already found in the snapshot", addressHash);
                }
                balanceMap.put(addressHash, addressAmount);
                preBalanceMap.putAll(balanceMap);
            }
            preBalanceMap.putAll(balanceMap); // copy the balance to preBalance
        } catch (Exception e) {
            log.error("Errors {}", e);
        }
    }

}
