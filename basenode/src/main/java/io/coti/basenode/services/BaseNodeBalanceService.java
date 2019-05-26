package io.coti.basenode.services;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.http.GetBalancesRequest;
import io.coti.basenode.http.GetBalancesResponse;
import io.coti.basenode.services.interfaces.IBalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class BaseNodeBalanceService implements IBalanceService {

    public BigDecimal getBalanceByAddress(Hash addressHash) {
        return  balanceMap.containsKey(addressHash) ? balanceMap.get(addressHash) : BigDecimal.ZERO;
    }

    protected Map<Hash, BigDecimal> balanceMap;

    public BigDecimal getPreBalanceByAddress(Hash addressHash) {
        return  preBalanceMap.containsKey(addressHash) ? preBalanceMap.get(addressHash) : BigDecimal.ZERO;
    }

    protected Map<Hash, BigDecimal> preBalanceMap;

    public void init() {
        balanceMap = new ConcurrentHashMap<>();
        preBalanceMap = new ConcurrentHashMap<>();
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public synchronized boolean checkBalancesAndAddToPreBalance(List<BaseTransactionData> baseTransactions) {
        Map<Hash, BigDecimal> balanceInChangeMap = new HashMap<>();
        Map<Hash, BigDecimal> preBalanceInChangeMap = new HashMap<>();
        for (BaseTransactionData baseTransactionData : baseTransactions) {

            BigDecimal amount = baseTransactionData.getAmount();
            Hash addressHash = baseTransactionData.getAddressHash();
            balanceInChangeMap.putIfAbsent(addressHash, balanceMap.containsKey(addressHash) ? balanceMap.get(addressHash) : BigDecimal.ZERO);
            preBalanceInChangeMap.putIfAbsent(addressHash, preBalanceMap.containsKey(addressHash) ? preBalanceMap.get(addressHash) : BigDecimal.ZERO);
            if (amount.add(balanceInChangeMap.get(addressHash)).signum() < 0) {
                log.error("Error in Balance check. Address {}  amount {} current Balance {} ", addressHash,
                        amount, balanceInChangeMap.get(addressHash));
                return false;
            }
            if (amount.add(preBalanceInChangeMap.get(addressHash)).signum() < 0) {
                log.error("Error in PreBalance check. Address {}  amount {} current PreBalance {} ", addressHash,
                        amount, preBalanceInChangeMap.get(addressHash));
                return false;
            }
            preBalanceInChangeMap.put(addressHash, amount.add(preBalanceInChangeMap.get(addressHash)));
        }
        preBalanceInChangeMap.forEach((addressHash, preBalanceInChange) -> {
            preBalanceMap.put(addressHash, preBalanceInChange);
            continueHandleBalanceChanges(addressHash);
        });
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
                throw new IllegalArgumentException("ClusterStamp or database are corrupted.");
            }
        });
        balanceMap.forEach((hash, bigDecimal) -> {
            if (bigDecimal.signum() == -1) {
                log.error("Balance Validation failed!");
                throw new IllegalArgumentException("ClusterStamp or database are corrupted.");
            }
        });
        log.info("Balance Validation completed");
    }

    @Override
    public void updateBalanceFromClusterStamp(Hash addressHash, BigDecimal amount) throws Exception {
        if (balanceMap.containsKey(addressHash)) {
            log.error("The address {} was already found in the clusterstamp", addressHash);
            throw new Exception(String.format("The address %s was already found in the clusterstamp", addressHash));
        }
        balanceMap.put(addressHash, amount);
        log.trace("Loading from clusterstamp into inMem balance+preBalance address {} and amount {}", addressHash, amount);
    }

    @Override
    public void updatePreBalanceFromClusterStamp() {
        preBalanceMap.putAll(balanceMap);
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

}