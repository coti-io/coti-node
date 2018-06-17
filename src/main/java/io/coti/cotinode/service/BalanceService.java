package io.coti.cotinode.service;

import io.coti.cotinode.data.BaseTransactionData;
import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.model.BalanceDifferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class BalanceService {
    ConcurrentMap<Hash, Double> addressHashToAmountMapping;

    @Autowired
    BalanceDifferences balanceDifferences;

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

    public boolean isLegalTransaction(Hash hash) {
        return true;
    }

    public void addToPreBalance(TransactionData transactionData) {
    }

    public List<BaseTransactionData> getBalances(List<Hash> addressHashes) {
        return null;
    }

    public boolean addNewAddress(Hash addressHash) {
        return false;
    }
}
