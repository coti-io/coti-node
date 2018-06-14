package io.coti.cotinode;

import io.coti.cotinode.data.BaseTransactionObject;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.service.BalanceService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BalanceServiceTests {

    @Autowired
    BalanceService balanceService;

    @Before
    public void init() {
        balanceService = new BalanceService();
    }

    @Test
    public void getBalances_EmptyBalancesList_ReturnsEmptyList() {
        List<BaseTransactionObject> balances = balanceService.getBalances(new ArrayList<>());
        Assert.assertTrue(balances.equals(new ArrayList<>()));
    }

    @Test
    public void getBalances_StoreAndRetrieveBalances_ReturnsBalances() {
        balanceService.addToBalance(new TransactionData(
                Arrays.asList(
                        new BaseTransactionObject("Address1".getBytes(), 12.5),
                        new BaseTransactionObject("Address2".getBytes(), 44))));

        List<BaseTransactionObject> balances =
                balanceService.getBalances(Arrays.asList(
                        "Address1".getBytes(),
                        "Address2".getBytes()));
        List<BaseTransactionObject> expectedBalances = Arrays.asList(
                new BaseTransactionObject("Address1".getBytes(), 12.5),
                new BaseTransactionObject("Address2".getBytes(), 44));
        Assert.assertTrue(balances.equals(expectedBalances));
    }
}
