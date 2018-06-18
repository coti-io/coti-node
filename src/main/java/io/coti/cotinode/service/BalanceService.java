package io.coti.cotinode.service;

import io.coti.cotinode.data.BaseTransactionData;
import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class BalanceService {
    ConcurrentMap<Hash, Double> addressHashToAmountMapping;

    public BalanceService() {
        addressHashToAmountMapping = new ConcurrentHashMap<>();
    }

    public void addToBalance(TransactionData transactionData) {

    }

    private void updateAddressBalance(Hash address, double amount) {
        addressHashToAmountMapping.
                merge(address, amount,
                        (oldAmount, additionalAmount) -> oldAmount + additionalAmount);
    }


    public List<BaseTransactionData> getBalances(List<Hash> addressHashes) {
        List<BaseTransactionData> requestedBalances = new LinkedList<>();
        for (Hash addressHash :
                addressHashes) {
            requestedBalances.add(
                    new BaseTransactionData(
                            addressHash,
                            addressHashToAmountMapping.getOrDefault(addressHash, 0.0)));
        }
        return requestedBalances;
    }

    public boolean isLegalTransaction(Hash hash) {
        return true;
    }

    public void addToPreBalance(TransactionData transactionData) {
    }
}
