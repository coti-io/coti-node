package io.coti.cotinode.service;

import io.coti.cotinode.data.BaseTransactionObject;
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
        for (BaseTransactionObject baseTransaction :
                transactionData.baseTransactions) {
            updateAddressBalance(baseTransaction.getAddressHash(), baseTransaction.getAmount());
        }
    }

    private void updateAddressBalance(Hash address, double amount) {
        addressHashToAmountMapping.
                merge(address, amount,
                        (oldAmount, additionalAmount) -> oldAmount + additionalAmount);
    }


    public List<BaseTransactionObject> getBalances(List<Hash> addressHashes) {
        List<BaseTransactionObject> requestedBalances = new LinkedList<>();
        for (Hash addressHash :
                addressHashes) {
            requestedBalances.add(
                    new BaseTransactionObject(addressHash, addressHashToAmountMapping.getOrDefault(addressHash, 0.0)));
        }
        return requestedBalances;
    }

    public boolean isLegalTransaction(Hash hash) {
        return true;
    }

    public void addToPreBalance(TransactionData transactionData) {
    }

    public void revertTransaction(TransactionData transactionData) {
    }

    public void shutdown() {
        
    }
}
