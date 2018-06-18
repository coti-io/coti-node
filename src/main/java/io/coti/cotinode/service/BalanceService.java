package io.coti.cotinode.service;

import io.coti.cotinode.data.BaseTransactionData;
import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.model.Balances;
import io.coti.cotinode.model.Collection;
import io.coti.cotinode.model.PreBalances;
import io.coti.cotinode.service.interfaces.IBalanceService;
import io.coti.cotinode.storage.Interfaces.IDatabaseConnector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class BalanceService implements IBalanceService {

    private Map<Hash, Double> balanceMap;

    private Map<Hash, Double> preBalanceMap;

    @Autowired
    private Balances balances;

    @Autowired
    private PreBalances preBalances;

    @Autowired
    private IDatabaseConnector databaseConnector;

    private Map<Hash,Double> balanceMapFromDB;

    private Map<Hash , Double> preBalanceMapFromDB;

    private Hash lastInsertedHash;

    @PostConstruct
    private void init(){
        balanceMap = new ConcurrentHashMap<>();
        preBalanceMap = new ConcurrentHashMap<>();

        loadBalanceFromSnapshot();

        balanceMapFromDB =  (Map) databaseConnector.getFullMapFromDB("Balances");
        preBalanceMapFromDB =  (Map)databaseConnector.getFullMapFromDB("PreBalances");

        syncMap(balanceMapFromDB , balanceMap , balances);
        syncMap(preBalanceMapFromDB , preBalanceMap , preBalances);
    }

    /**
     * The task will be executed a first time after the initialDelay (because of the init() ) value – and it will continue to be executed according to the fixedDelay
     */
    @Scheduled(fixedDelay = 5000 , initialDelay = 1000)
    private void syncBalanceScheduled(){
        balanceMapFromDB =  (Map) databaseConnector.getMapAfterKeyFromDB("Balances",lastInsertedHash);
        preBalanceMapFromDB =  (Map)databaseConnector.getMapAfterKeyFromDB("PreBalances",lastInsertedHash);

        syncMap(balanceMapFromDB , balanceMap , balances);
        syncMap(preBalanceMapFromDB , preBalanceMap , preBalances);

    }


    private void syncMap(Map<Hash,Double> mapFromDB , Map<Hash,Double> mapToFill ,Collection<?> data){
        for(Map.Entry<Hash,Double> entry : mapFromDB.entrySet()){
            Hash key = entry.getKey();
            if(mapToFill.containsKey(key)){
                double newAmount = mapToFill.get(key) + entry.getValue();
                mapToFill.put(key,newAmount);
                log.debug("The hash {} was updated to " , key , newAmount);
            }
            else{
                mapToFill.put(entry.getKey(),entry.getValue());
                log.debug("The hash {} was inserted with value{}" , key , entry.getValue());
            }
        }
    }


    @Override
    public void loadBalanceFromSnapshot() {
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
                log.info("The hash {} was loaded from the snapshot with amount",addressHash,addressAmount);

                if (balanceMap.containsKey(addressHash)) {
                   // throw new Exception(String.format("Double address found in CSV file: %s", addressHash));
                    log.error("The address {} was already found in the snapshot",addressHash);
                }
                balanceMap.put(addressHash, addressAmount);

            }
            preBalanceMap.putAll(balanceMap); // copy the balance to preBalance
        } catch (Exception e) {
            log.error("Errors {}",e);
        }
    }

    public void updateBalanceFromPreBalance(TransactionData transactionData){
        if(transactionData.isTransactionConsensus() && transactionData.isDspConsensus()){
            for(Map.Entry<Hash,Double> entry : preBalanceMap.entrySet() ){
                Hash transactionHash = entry.getKey();
                if(balanceMap.containsKey(transactionHash )){
                    balanceMap.put(transactionHash,preBalanceMap.get(transactionHash));
                }
                else{
                    // TODO : what todo here ?

                }
            }
        }
    }



    public void addToBalance(TransactionData transactionData) {
        // on the base transaction
        for(BaseTransactionData baseTransaction : transactionData.getBaseTransactionsData()){
            balanceMap.put(baseTransaction.getHash(),baseTransaction.getValue());
        }

    }

    public void addToPreBalance(TransactionData transactionData) {

        for(BaseTransactionData baseTransaction : transactionData.getBaseTransactionsData()){
            preBalanceMap.put(baseTransaction.getHash(),baseTransaction.getValue());
        }
        lastInsertedHash = transactionData.getBaseTransactionsData().get(transactionData.getBaseTransactionsData().size()-1).getHash();
    }

    public void updateAddressBalance(Hash address, double amount) {
        preBalanceMap.put(address,amount);
    }

    public Map<Hash,Double> getBalances(List<Hash> addressHashes) {
        Map<Hash,Double> requestedBalances = new HashMap<>();
        for (Hash addressHash : addressHashes) {
            requestedBalances.put(addressHash,preBalanceMap.get(addressHash));
        }
        return requestedBalances;
    }


    // TODO: not in use
    public synchronized boolean balanceCheck(TransactionData data){
        return true;
    }

    public synchronized boolean preBalanceCheck(TransactionData data){
        for(BaseTransactionData baseTransaction : data.getBaseTransactionsData()){
            double currentPreBalance = preBalanceMap.get(baseTransaction.getHash());
            if((currentPreBalance +  baseTransaction.getValue()) < 0){
                log.info("Unfortunately the Transaction {} with baseTransaction {} has the amount {} but the current balance is {} ",
                        data.getHash(),baseTransaction.getHash() , baseTransaction.getValue() ,currentPreBalance);
                return false;
            }
        }
        log.info("Balance check completed successfully");

        return true;
    }




    ////// ################# SETTERS AND GETTERS ######################

    public Map<Hash, Double> getBalanceMap() {
        return balanceMap;
    }

    public void setBalanceMap(Map<Hash, Double> balanceMap) {
        this.balanceMap = balanceMap;
    }

    public Map<Hash, Double> getPreBalanceMap() {
        return preBalanceMap;
    }

    public void setPreBalanceMap(Map<Hash, Double> preBalanceMap) {
        this.preBalanceMap = preBalanceMap;
    }


}
