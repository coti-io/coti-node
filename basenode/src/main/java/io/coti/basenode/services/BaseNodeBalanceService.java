package io.coti.basenode.services;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.ClusterStampData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.*;
import io.coti.basenode.model.ClusterStamps;
import io.coti.basenode.services.interfaces.IBalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class BaseNodeBalanceService implements IBalanceService {
    protected Map<Hash, BigDecimal> balanceMap;
    protected Map<Hash, BigDecimal> preBalanceMap;
    @Autowired
    private ClusterStamps clusterStamps;

    @Value("${recovery.server.address}")
    private String recoveryServerAddress;

    public void init() throws Exception {

        balanceMap = new ConcurrentHashMap<>();
        preBalanceMap = new ConcurrentHashMap<>();
        // TODO Consider moving this to extending class if not necessary in all nodes. -->
        ClusterStampData lastClusterStampData = getLastClusterStamp();
        if(lastClusterStampData != null) {
            loadBalanceFromClusterStamp(lastClusterStampData);
        }
        else {
            loadBalanceFromSnapshot();
        }
        // TODO <--
        log.info("{} is up", this.getClass().getSimpleName());
    }

    protected void loadBalanceFromClusterStamp(ClusterStampData lastClusterStampData){
        //implemented in extending class
    }

    private void loadBalanceFromSnapshot() throws Exception {

        String snapshotFileLocation = "snapshot.csv";
        File snapshotFile = new File(snapshotFileLocation);

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(snapshotFile))) {

            String line;

            while ((line = bufferedReader.readLine()) != null) {

                String[] addressDetails = line.split(",");
                if (addressDetails.length != 2) {
                    throw new Exception("Bad csv file format");
                }
                Hash addressHash = new Hash(addressDetails[0]);
                BigDecimal addressAmount = new BigDecimal(addressDetails[1]);
                log.trace("The hash {} was loaded from the snapshot with amount {}", addressHash, addressAmount);

                if (balanceMap.containsKey(addressHash)) {
                    log.error("The address {} was already found in the snapshot", addressHash);
                    throw new Exception(String.format("The address %s was already found in the snapshot", addressHash));
                }
                balanceMap.put(addressHash, addressAmount);
                log.trace("Loading from snapshot into inMem balance+preBalance address {} and amount {}",
                        addressHash, addressAmount);
            }
            log.info("Snapshot is finished");
            preBalanceMap.putAll(balanceMap);
        } catch (Exception e) {
            log.error("Errors on snapshot loading: {}", e);
            throw e;
        }
    }

    private ClusterStampData getLastClusterStamp() {

        long totalConfirmedTransactionsPriorClusterStamp;

        ClusterStampData localClusterStampData = clusterStamps.getByHash(new Hash(""));

        if (localClusterStampData != null) {
            totalConfirmedTransactionsPriorClusterStamp = localClusterStampData.getTotalConfirmedTransactionsPriorClusterStamp();
        }
        else {
            totalConfirmedTransactionsPriorClusterStamp = 0;
        }

        if(!recoveryServerAddress.isEmpty()) {
            RestTemplate restTemplate = new RestTemplate();
            ClusterStampData lastClusterStampData =
                    restTemplate.postForObject(
                            recoveryServerAddress + "/getLastClusterStamp",
                            totalConfirmedTransactionsPriorClusterStamp,
                            ClusterStampData.class);

            if(lastClusterStampData != null) {
                log.info("Received last cluster stamp for total confirmed transactions: {}", localClusterStampData.getTotalConfirmedTransactionsPriorClusterStamp());
                return lastClusterStampData;
            }
        }

        return localClusterStampData;
    }

    @Override
    public synchronized boolean checkBalancesAndAddToPreBalance(List<BaseTransactionData> baseTransactionDatas) {
        Map<Hash, BigDecimal> preBalanceChanges = new HashMap<>();
        for (BaseTransactionData baseTransactionData : baseTransactionDatas) {

            BigDecimal amount = baseTransactionData.getAmount();
            Hash addressHash = baseTransactionData.getAddressHash();
            BigDecimal balance = balanceMap.containsKey(addressHash) ? balanceMap.get(addressHash) : BigDecimal.ZERO;
            BigDecimal preBalance = preBalanceMap.containsKey(addressHash) ? preBalanceMap.get(addressHash) : BigDecimal.ZERO;
            if (amount.add(balance).signum() < 0) {
                log.error("Error in Balance check. Address {}  amount {} current Balance {} ", addressHash,
                        amount, balance);
                return false;
            }
            if (amount.add(preBalance).signum() < 0) {
                log.error("Error in PreBalance check. Address {}  amount {} current PreBalance {} ", addressHash,
                        amount, preBalance);
                return false;
            }
            preBalanceChanges.put(addressHash, amount.add(preBalance));
        }
        preBalanceChanges.forEach((addressHash, preBalance) -> {
            preBalanceMap.put(addressHash, preBalance);
            continueHandleBalanceChanges(addressHash);
        });
        return true;
    }

    @Override
    public void continueHandleBalanceChanges(Hash addressHash) {
        //implemented in extending class
    }

    @Override
    public ResponseEntity<GetBalancesResponse> getBalances(GetBalancesRequest getBalancesRequest) {
        GetBalancesResponse getBalancesResponse = new GetBalancesResponse();
        BigDecimal balance;
        BigDecimal preBalance;
        for (Hash hash : getBalancesRequest.getAddresses()) {
            balance = balanceMap.containsKey(hash) ? balanceMap.get(hash) : new BigDecimal(0);
            preBalance = preBalanceMap.containsKey(hash) ? preBalanceMap.get(hash) : new BigDecimal(0);
            getBalancesResponse.addAddressBalanceToResponse(hash, balance, preBalance);

        }
        return ResponseEntity.status(HttpStatus.OK).body(getBalancesResponse);
    }

    @Override
    public void rollbackBaseTransactions(TransactionData transactionData) {
        transactionData.getBaseTransactions().forEach(baseTransactionData ->
                preBalanceMap.computeIfPresent(baseTransactionData.getAddressHash(), (addressHash, amount) -> amount.add(baseTransactionData.getAmount().negate()))
        );
    }

    @Override
    public void validateBalances() {
        preBalanceMap.forEach((hash, bigDecimal) -> {
            if (bigDecimal.signum() == -1) {
                log.error("PreBalance Validation failed!");
                throw new IllegalArgumentException("Snapshot or database are corrupted.");
            }
        });
        balanceMap.forEach((hash, bigDecimal) -> {
            if (bigDecimal.signum() == -1) {
                log.error("Balance Validation failed!");
                throw new IllegalArgumentException("Snapshot or database are corrupted.");
            }
        });
        log.info("Balance Validation completed");
    }

    @Override
    public void updateBalance(Hash addressHash, BigDecimal amount) {
        balanceMap.computeIfPresent(addressHash, (currentHash, currentAmount) ->
                currentAmount.add(amount));
        balanceMap.putIfAbsent(addressHash, amount);
    }

    @Override
    public void updatePreBalance(Hash addressHash, BigDecimal amount) {
        preBalanceMap.computeIfPresent(addressHash, (currentHash, currentAmount) ->
                currentAmount.add(amount));
        preBalanceMap.putIfAbsent(addressHash, amount);
    }

    @Override
    public void updateBalanceAndPreBalanceMap(Map<Hash, BigDecimal> balanceMap) {
        this.balanceMap = balanceMap;
        preBalanceMap.putAll(balanceMap);
    }

    @Override
    public Map<Hash, BigDecimal> getBalanceMap() {
        return balanceMap;
    }


}