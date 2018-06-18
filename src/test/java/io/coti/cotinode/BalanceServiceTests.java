package io.coti.cotinode;

import io.coti.cotinode.data.BaseTransactionData;
import io.coti.cotinode.data.BaseTransactionObject;
import io.coti.cotinode.data.Hash;
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

//    @Test
//    public void getBalances_EmptyBalancesList_ReturnsEmptyList() {
//        List<BaseTransactionData> balances = balanceService.getBalances(new ArrayList<>());
//        Assert.assertTrue(balances.equals(new ArrayList<>()));
//    }


}
