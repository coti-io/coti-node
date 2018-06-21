package io.coti.cotinode;

import io.coti.cotinode.service.BalanceService;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

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
