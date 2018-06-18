package io.coti.cotinode.service;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.http.GetBalancesRequest;
import io.coti.cotinode.http.GetBalancesResponse;
import io.coti.cotinode.model.BalanceDifferences;
import io.coti.cotinode.service.interfaces.IBalanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class BalanceService implements IBalanceService {
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

    public GetBalancesResponse getBalances(GetBalancesRequest request) {
        return new GetBalancesResponse(
                HttpStatus.OK,
                "");
    }

    public boolean addNewAddress(Hash addressHash) {
        return false;
    }
}
