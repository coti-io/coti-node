package io.coti.basenode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.http.GetBalancesRequest;
import io.coti.basenode.http.GetBalancesResponse;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.interfaces.IBalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class BaseNodeBalanceService implements IBalanceService {
    protected Map<Hash, BigDecimal> balanceMap;
    protected Map<Hash, BigDecimal> preBalanceMap;
    private Map<Hash, List<Hash>> addressHashToPreBalanceGapTransactionHashes;

    @Autowired
    TransactionHelper transactionHelper;
    @Autowired
    Transactions transactions;

    public void init() throws Exception {
        balanceMap = new ConcurrentHashMap<>();
        preBalanceMap = new ConcurrentHashMap<>();
        addressHashToPreBalanceGapTransactionHashes = new ConcurrentHashMap<>();
        loadBalanceFromSnapshot();
        log.info("{} is up", this.getClass().getSimpleName());
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

    @Override
    public synchronized boolean checkBalancesAndAddToPreBalance(TransactionData transactionData) {

        for (BaseTransactionData inputBaseTransactionData : transactionData.getInputBaseTransactions()) {

            BigDecimal amount = inputBaseTransactionData.getAmount();
            Hash addressHash = inputBaseTransactionData.getAddressHash();
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

            continueHandleBalanceChanges(addressHash);
        }

        updateBalanceAndPreBalanceConditionally(transactionData);

        return true;
    }

    @Override
    public void continueHandleBalanceChanges(Hash addressHash) {
    }

    @Override
    public ResponseEntity<GetBalancesResponse> getBalances(GetBalancesRequest getBalancesRequest) {
        GetBalancesResponse getBalancesResponse = new GetBalancesResponse();
        BigDecimal balance;
        BigDecimal preBalance;
        for (Hash hash : getBalancesRequest.getAddresses()) {
            balance = balanceMap.containsKey(hash) ? balanceMap.get(hash) : new BigDecimal(0);
            preBalance = preBalanceMap.containsKey(hash) ? preBalanceMap.get(hash) : new BigDecimal(0);
            if(getBalancesRequest.isIncludePreBalanceGapTransactions() && addressHashToPreBalanceGapTransactionHashes.containsKey(hash)) {
                List<Hash> preBalanceGapTransactionHashes =  addressHashToPreBalanceGapTransactionHashes.get(hash);
                List<TransactionData> preBalanceGapTransactions = new ArrayList<>();
                    preBalanceGapTransactionHashes.forEach(
                    preBalanceGapTransactionHash ->
                        preBalanceGapTransactions.add(transactions.getByHash(preBalanceGapTransactionHash))
                    );
                getBalancesResponse.addAddressBalanceToResponse(hash, balance, preBalance, preBalanceGapTransactions);
            }
            else {
                getBalancesResponse.addAddressBalanceToResponse(hash, balance, preBalance);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(getBalancesResponse);
    }

    @Override
    public void rollbackBaseTransactions(TransactionData transactionData) {

        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
                preBalanceMap.computeIfPresent(baseTransactionData.getAddressHash(), (addressHash, amount) -> amount.add(baseTransactionData.getAmount().negate()));
                addressHashToPreBalanceGapTransactionHashes.get(baseTransactionData.getAddressHash()).remove(transactionData.getHash());
            }
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
    public synchronized void updateBalanceAndPreBalanceConditionally(TransactionData transactionData) {

        if(transactionData.getType() == TransactionType.ZeroSpend) {
            return;
        }
        boolean isConfirmed = transactionHelper.isConfirmed(transactionData);

        for (BaseTransactionData baseTransactionData : transactionData.getBaseTransactions()) {
            if(isConfirmed) {
                updateBalance(baseTransactionData.getAddressHash(), baseTransactionData.getAmount(), transactionData.getHash());
            }
            if(baseTransactionData instanceof InputBaseTransactionData && !isConfirmed) {
                updatePreBalance(baseTransactionData.getAddressHash(), baseTransactionData.getAmount(), transactionData.getHash(), false);
            }
            if(baseTransactionData instanceof OutputBaseTransactionData && isConfirmed) {
                updatePreBalance(baseTransactionData.getAddressHash(), baseTransactionData.getAmount(), transactionData.getHash(), true);
            }
        }
    }

    private void updateBalance(Hash addressHash, BigDecimal amount, Hash transactionHash) {
        balanceMap.computeIfPresent(addressHash, (currentHash, currentAmount) ->
                currentAmount.add(amount));
        balanceMap.putIfAbsent(addressHash, amount);

        List<Hash> transactionHashList = addressHashToPreBalanceGapTransactionHashes.get(addressHash);

        if( transactionHashList != null ) {
            transactionHashList.remove(transactionHash);

            if( !transactionHashList.isEmpty() ) {
                addressHashToPreBalanceGapTransactionHashes.put(addressHash, transactionHashList);
            }
            else {
                addressHashToPreBalanceGapTransactionHashes.remove(addressHash);
            }
        }
    }

    private void updatePreBalance(Hash addressHash, BigDecimal amount, Hash transactionHash, boolean isConfirmed) {
        preBalanceMap.computeIfPresent(addressHash, (currentHash, currentAmount) ->
                currentAmount.add(amount));
        preBalanceMap.putIfAbsent(addressHash, amount);

        if( !isConfirmed ) {

            List<Hash> transactionHashList = addressHashToPreBalanceGapTransactionHashes.get(addressHash);

            if( transactionHashList == null ) {
                transactionHashList = new ArrayList<>();
            }

            if( !transactionHashList.contains(transactionHash) ) {
                transactionHashList.add(transactionHash);
            }

            addressHashToPreBalanceGapTransactionHashes.put(addressHash, transactionHashList);
        }
    }
}