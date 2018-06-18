package io.coti.cotinode;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.service.BalanceService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BalanceDataServiceTests {

    @Autowired
    private BalanceService balanceService;


    @Test
    public void getBalances_EmptyBalancesList_ReturnsEmptyList() {
        Map<Hash, Double> balances = balanceService.getBalances(new ArrayList<>());
        Assert.assertTrue(balances.equals(new ArrayList<>()));
    }

    @Test
    public void getBalances_StoreAndRetrieveBalances_ReturnsBalances() {
        balanceService.addToBalance(new TransactionData(new Hash("TransactionData 1".getBytes())));

        Map<Hash, Double> balances = balanceService.getBalances(Arrays.asList(new Hash("Address1".getBytes()), new Hash("Address2".getBytes())));
        Map<Hash, Double> expectedBalances = new ConcurrentHashMap<>();
        expectedBalances.put(new Hash("Address1".getBytes()), 12.5);
        expectedBalances.put(new Hash("Address2".getBytes()), 44.0);
        Assert.assertTrue(balances.equals(expectedBalances));
    }
}
