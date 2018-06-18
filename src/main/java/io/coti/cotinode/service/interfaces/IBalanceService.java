package io.coti.cotinode.service.interfaces;

import io.coti.cotinode.data.BaseTransactionData;
import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;

import java.util.List;
import java.util.Map;

public interface IBalanceService {

    public void loadBalanceFromSnapshot();

    public Map<Hash,Double> getBalances(List<Hash> addressHashes);

    public void addToBalance(TransactionData transactionData);

    public void updateAddressBalance(Hash address, double amount);

    public void addToPreBalance(TransactionData transactionData) ;

    public boolean preBalanceCheck(TransactionData data);

    public void updateBalanceFromPreBalance(TransactionData transactionData);


}
