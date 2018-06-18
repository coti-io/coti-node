package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.database.Interfaces.IDatabaseConnector;
import io.coti.cotinode.http.GetBalancesResponse;
import io.coti.cotinode.model.BalanceDifferences;
import io.coti.cotinode.model.Collection;
import io.coti.cotinode.model.PreBalanceDifferences;
import io.coti.cotinode.service.interfaces.IBalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class BalanceService implements IBalanceService {

    @Autowired
    private BalanceDifferences balances;

    @Autowired
    private PreBalanceDifferences preBalances;

    @Autowired
    private IDatabaseConnector databaseConnector;

    private Map<Hash, Double> balanceMap ;  // TODO: HasH - > preBalanceDifference

    private Map<Hash, Double> preBalanceMap;

    private Map<Hash, Double> balanceMapFromDB; // in  the db the value is BalanceDifferences

    private Map<Hash, Double> preBalanceMapFromDB;

    private Hash lastInsertedHash;

    private static final String BALANCES_COLOUMN_FAMILY_NAME = "";
    private static final String PRE_BALANCES_COLOUMN_FAMILY_NAME = "";

    @PostConstruct
    private void init() {
        balanceMap = new ConcurrentHashMap<>();
        preBalanceMap = new ConcurrentHashMap<>();

        loadBalanceFromSnapshot();

        balanceMapFromDB = (Map) databaseConnector.getFullMapFromDB("Balances"); // TODO: should we do that here?
        preBalanceMapFromDB = (Map) databaseConnector.getFullMapFromDB("PreBalances");

        syncMap(balanceMapFromDB, balanceMap, balances);
        syncMap(preBalanceMapFromDB, preBalanceMap, preBalances);
    }

    /**
     * The task will be executed a first time after the initialDelay (because of the init() ) value – and it will continue to be executed according to the fixedDelay
     */
    @Scheduled(fixedDelay = 5000, initialDelay = 1000)
    private void syncBalanceScheduled() {
        balanceMapFromDB = (Map) databaseConnector.getMapAfterKeyFromDB("Balances", lastInsertedHash);
        preBalanceMapFromDB = (Map) databaseConnector.getMapAfterKeyFromDB("PreBalances", lastInsertedHash);

        syncMap(balanceMapFromDB, balanceMap, balances);
        syncMap(preBalanceMapFromDB, preBalanceMap, preBalances);

    }


    private void syncMap(Map<Hash, Double> mapFromDB, Map<Hash, Double> mapToFill, Collection<?> data) {
        for (Map.Entry<Hash, Double> entry : mapFromDB.entrySet()) {
            Hash key = entry.getKey();
            if (mapToFill.containsKey(key)) {
                double newAmount = mapToFill.get(key) + entry.getValue();
                mapToFill.put(key, newAmount);
                log.debug("The hash {} was updated to ", key, newAmount);
            } else {
                mapToFill.put(entry.getKey(), entry.getValue());
                log.debug("The hash {} was inserted with value{}", key, entry.getValue());
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
                Hash addressHash = new Hash(addressDetails[0].getBytes());
                Double addressAmount = Double.parseDouble(addressDetails[1]);
                log.info("The hash {} was loaded from the snapshot with amount", addressHash, addressAmount);

                if (balanceMap.containsKey(addressHash)) {
                    // throw new Exception(String.format("Double address found in CSV file: %s", addressHash));
                    log.error("The address {} was already found in the snapshot", addressHash);
                }
                balanceMap.put(addressHash, addressAmount);

            }
            preBalanceMap.putAll(balanceMap); // copy the balance to preBalance
        } catch (Exception e) {
            log.error("Errors {}", e);
        }
    }

    public void updateBalanceFromPreBalance(TransactionData transactionData) { // (Hash hash)
        if (transactionData.isTransactionConsensus() && transactionData.isDspConsensus()) {
            for (Map.Entry<Hash, Double> entry : preBalanceMap.entrySet()) {
                Hash transactionHash = entry.getKey();
                if (balanceMap.containsKey(transactionHash)) {
                    balanceMap.put(transactionHash, preBalanceMap.get(transactionHash));
                    preBalanceMap.remove(transactionHash);
                } else {
                    // TODO : what todo here ?

                }
            }
        }
    }


    public void addToBalance(TransactionData transactionData) {
        // on the base transaction
        for (BaseTransactionData baseTransaction : transactionData.getBaseTransactionsData()) {
            balanceMap.put(baseTransaction.getHash(), baseTransaction.getValue());
        }

    }

    public void addToPreBalance(TransactionData transactionData) {

        for (BaseTransactionData baseTransaction : transactionData.getBaseTransactionsData()) {
            preBalanceMap.put(baseTransaction.getHash(), baseTransaction.getValue());
        }
        lastInsertedHash = transactionData.getBaseTransactionsData().get(transactionData.getBaseTransactionsData().size() - 1).getHash();
    }

    public void updateAddressBalance(Hash address, double amount) {
        preBalanceMap.put(address, amount);
    }

    public Map<Hash, Double> getBalances(List<Hash> addressHashes) {
        Map<Hash, Double> requestedBalances = new HashMap<>();
        for (Hash addressHash : addressHashes) {
            requestedBalances.put(addressHash, preBalanceMap.get(addressHash));


        public synchronized boolean preBalanceCheck (TransactionData data){
            for (BaseTransactionData baseTransaction : data.getBaseTransactionsData()) {
                double currentPreBalance = preBalanceMap.get(baseTransaction.getHash());
                if ((currentPreBalance + baseTransaction.getValue()) < 0) {
                    log.info("Unfortunately the Transaction {} with baseTransaction {} has the amount {} but the current balance is {} ",
                            data.getHash(), baseTransaction.getHash(), baseTransaction.getValue(), currentPreBalance);
                    return false;
                }
            }
            log.info("Balance check completed successfully");

            return true;
        }

        public synchronized boolean balanceCheck (TransactionData data){     // TODO: not in use
            return true;
        }
            ////// ################# SETTERS AND GETTERS ######################

            public Map<Hash, Double> getBalanceMap () {
                return balanceMap;
                HttpStatus.OK,
                "");
            }

            public void setBalanceMap (Map < Hash, Double > balanceMap){
                this.balanceMap = balanceMap;
            }

            public Map<Hash, Double> getPreBalanceMap () {
                return preBalanceMap;
            }
            public void setPreBalanceMap (Map < Hash, Double > preBalanceMap){
                this.preBalanceMap = preBalanceMap;
            }


        }
