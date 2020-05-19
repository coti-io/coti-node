package io.coti.basenode.services;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.exceptions.BalanceException;
import io.coti.basenode.http.GetBalancesRequest;
import io.coti.basenode.http.GetBalancesResponse;
import io.coti.basenode.http.GetTokenBalancesRequest;
import io.coti.basenode.http.GetTokenBalancesResponse;
import io.coti.basenode.http.data.AddressBalance;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.ICurrencyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class BaseNodeBalanceService implements IBalanceService {

    protected Map<Hash, Map<Hash, BigDecimal>> balanceMap;
    protected Map<Hash, Map<Hash, BigDecimal>> preBalanceMap;
    @Autowired
    protected ICurrencyService currencyService;

    public void init() {
        balanceMap = new ConcurrentHashMap<>();
        preBalanceMap = new ConcurrentHashMap<>();
        log.info("{} is up", this.getClass().getSimpleName());
    }

    @Override
    public synchronized boolean checkBalancesAndAddToPreBalance(List<BaseTransactionData> baseTransactions) {
        Map<Hash, Map<Hash, BigDecimal>> balanceInChangeMap = new HashMap<>();
        Map<Hash, Map<Hash, BigDecimal>> preBalanceInChangeMap = new HashMap<>();
        for (BaseTransactionData baseTransactionData : baseTransactions) {

            BigDecimal amount = baseTransactionData.getAmount();
            Hash addressHash = baseTransactionData.getAddressHash();
            Hash currencyHash = getNativeCurrencyHashIfNull(baseTransactionData.getCurrencyHash());
            balanceInChangeMap.putIfAbsent(addressHash, new ConcurrentHashMap<>());
            balanceInChangeMap.get(addressHash).putIfAbsent(currencyHash, getBalance(addressHash, currencyHash));
            preBalanceInChangeMap.putIfAbsent(addressHash, new ConcurrentHashMap<>());
            preBalanceInChangeMap.get(addressHash).putIfAbsent(currencyHash, getPreBalance(addressHash, currencyHash));
            if (amount.add(balanceInChangeMap.get(addressHash).get(currencyHash)).signum() < 0) {
                log.error("Error in Balance check. Address {},  amount {}, current Balance {} ", addressHash,
                        amount, balanceInChangeMap.get(addressHash).get(currencyHash));
                return false;
            }
            if (amount.add(preBalanceInChangeMap.get(addressHash).get(currencyHash)).signum() < 0) {
                log.error("Error in PreBalance check. Address {},  amount {}, current PreBalance {} ", addressHash,
                        amount, preBalanceInChangeMap.get(addressHash).get(currencyHash));
                return false;
            }
            preBalanceInChangeMap.get(addressHash).put(currencyHash, amount.add(preBalanceInChangeMap.get(addressHash).get(currencyHash)));
        }
        preBalanceInChangeMap.forEach((addressHash, currencyHashPreBalanceMap) -> {
            preBalanceMap.putIfAbsent(addressHash, new ConcurrentHashMap<>());
            currencyHashPreBalanceMap.forEach((currencyHash, preBalanceInChange) -> {
                preBalanceMap.get(addressHash).put(currencyHash, preBalanceInChange);
                continueHandleBalanceChanges(addressHash, currencyHash);
            });

        });
        return true;
    }

    @Override
    public void continueHandleBalanceChanges(Hash addressHash, Hash currencyHash) {
        // implemented by the sub classes
    }

    @Override
    public ResponseEntity<GetBalancesResponse> getBalances(GetBalancesRequest getBalancesRequest) {
        Hash nativeCurrencyHash = currencyService.getNativeCurrencyHash();
        GetBalancesResponse getBalancesResponse = new GetBalancesResponse();
        BigDecimal balance;
        BigDecimal preBalance;
        for (Hash addressHash : getBalancesRequest.getAddresses()) {
            balance = getBalance(addressHash, nativeCurrencyHash);
            preBalance = getPreBalance(addressHash, nativeCurrencyHash);
            getBalancesResponse.addAddressBalanceToResponse(addressHash, balance, preBalance);
        }
        return ResponseEntity.status(HttpStatus.OK).body(getBalancesResponse);
    }

    @Override
    public ResponseEntity<GetTokenBalancesResponse> getTokenBalances(GetTokenBalancesRequest getTokenBalancesRequest) {
        Map<Hash, Map<Hash, AddressBalance>> tokenToAddressesBalance = new HashMap<>();

        getTokenBalancesRequest.getAddresses().forEach(address -> {
            Map<Hash, BigDecimal> addressToPreBalanceMap = preBalanceMap.get(address);
            if (addressToPreBalanceMap != null) {
                addressToPreBalanceMap.entrySet().forEach(entry -> {
                    Hash currencyHash = entry.getKey();
                    BigDecimal preBalance = entry.getValue();
                    tokenToAddressesBalance.putIfAbsent(currencyHash, new HashMap<>());
                    tokenToAddressesBalance.get(currencyHash).putIfAbsent(address, new AddressBalance(getBalance(address, currencyHash), preBalance));
                });
            }
        });
        tokenToAddressesBalance.remove(currencyService.getNativeCurrencyHash());

        return ResponseEntity.status(HttpStatus.OK).body(new GetTokenBalancesResponse(tokenToAddressesBalance));
    }

    @Override
    public void rollbackBaseTransactions(TransactionData transactionData) {
        transactionData.getBaseTransactions().forEach(baseTransactionData -> {
            Hash currencyHash = getNativeCurrencyHashIfNull(baseTransactionData.getCurrencyHash());
            preBalanceMap.computeIfPresent(baseTransactionData.getAddressHash(), (addressHash, currencyHashPreBalanceMap) -> {
                currencyHashPreBalanceMap.computeIfPresent(currencyHash, (currentCurrencyHash, currentAmount) ->
                        currentAmount.add(baseTransactionData.getAmount().negate())
                );
                return currencyHashPreBalanceMap;
            });
        });
    }

    @Override
    public void validateBalances() {
        preBalanceMap.forEach((hash, currencyHashPreBalanceMap) ->
                currencyHashPreBalanceMap.forEach((currencyHash, preBalance) -> {
                    if (preBalance.signum() == -1) {
                        throw new BalanceException(String.format("PreBalance Validation failed with preBalance %s for address %s and currency %s", preBalance, hash, currencyHash));
                    }
                })
        );
        balanceMap.forEach((hash, currencyHashBalanceMap) ->
                currencyHashBalanceMap.forEach((currencyHash, balance) -> {
                    if (balance.signum() == -1) {
                        throw new BalanceException(String.format("Balance Validation failed with balance %s for address %s and currency %s", balance, hash, currencyHash));
                    }
                })
        );
        log.info("Balance Validation completed");
    }

    @Override
    public void updateBalanceAndPreBalanceFromClusterStamp(Hash addressHash, Hash currencyHash, BigDecimal amount) {
        currencyHash = getNativeCurrencyHashIfNull(currencyHash);
        if (balanceMap.containsKey(addressHash) && balanceMap.get(addressHash).containsKey(currencyHash)) {
            log.error("The address {} for currency {} was already found in the clusterstamp", addressHash, currencyHash);
            throw new IllegalArgumentException(String.format("The address %s for currency %s was already found in the clusterstamp", addressHash, currencyHash));
        }
        balanceMap.putIfAbsent(addressHash, new ConcurrentHashMap<>());
        balanceMap.get(addressHash).put(currencyHash, amount);
        preBalanceMap.putIfAbsent(addressHash, new ConcurrentHashMap<>());
        preBalanceMap.get(addressHash).put(currencyHash, amount);
        log.trace("Loading from clusterstamp into inMem balance+preBalance address {} and amount {}", addressHash, amount);
    }

    @Override
    public void updateBalance(Hash addressHash, Hash currencyHash, BigDecimal amount) {
        updateBalance(addressHash, currencyHash, amount, balanceMap);
    }

    @Override
    public void updatePreBalance(Hash addressHash, Hash currencyHash, BigDecimal amount) {
        updateBalance(addressHash, currencyHash, amount, preBalanceMap);
    }

    private void updateBalance(Hash addressHash, Hash currencyHash, BigDecimal amount, Map<Hash, Map<Hash, BigDecimal>> balanceMap) {
        final Hash finalCurrencyHash = getNativeCurrencyHashIfNull(currencyHash);
        balanceMap.computeIfPresent(addressHash, (currentHash, currentCurrencyHashBalanceMap) -> {
            currentCurrencyHashBalanceMap.computeIfPresent(finalCurrencyHash, (currentCurrencyHash, currentBalance) -> currentBalance.add(amount));
            currentCurrencyHashBalanceMap.putIfAbsent(finalCurrencyHash, amount);
            return currentCurrencyHashBalanceMap;
        });
        balanceMap.putIfAbsent(addressHash, getInitialCurrencyHashBalanceMap(finalCurrencyHash, amount));
    }

    private Map<Hash, BigDecimal> getInitialCurrencyHashBalanceMap(Hash currencyHash, BigDecimal amount) {
        Map<Hash, BigDecimal> currencyHashBalanceMap = new ConcurrentHashMap<>();
        currencyHashBalanceMap.put(currencyHash, amount);
        return currencyHashBalanceMap;
    }

    @Override
    public BigDecimal getBalance(Hash addressHash, Hash currencyHash) {
        return getBalance(addressHash, currencyHash, balanceMap);
    }

    @Override
    public BigDecimal getPreBalance(Hash addressHash, Hash currencyHash) {
        return getBalance(addressHash, currencyHash, preBalanceMap);
    }

    private BigDecimal getBalance(Hash addressHash, Hash
            currencyHash, Map<Hash, Map<Hash, BigDecimal>> balanceMap) {
        return new BigDecimal(Optional.ofNullable(Optional.ofNullable(balanceMap.get(addressHash)).orElse(new ConcurrentHashMap<>()).get(getNativeCurrencyHashIfNull(currencyHash))).orElse(BigDecimal.ZERO).toString());
    }

    protected Hash getNativeCurrencyHashIfNull(Hash currencyHash) {
        return Optional.ofNullable(currencyHash).orElse(currencyService.getNativeCurrencyHash());
    }

    @Override
    public TreeMap<Hash, BigDecimal> getSortedBalance(Hash currencyHash) {
        TreeMap<Hash, BigDecimal> currencySortedMap = new TreeMap();
        balanceMap.get(currencyHash).forEach((address, balance)-> {
            currencySortedMap.put(address, balance);
        });
        return currencySortedMap;
    }

}