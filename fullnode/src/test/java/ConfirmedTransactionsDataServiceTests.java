import io.coti.fullnode.service.BalanceService;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

public class ConfirmedTransactionsDataServiceTests {

    @Autowired
    BalanceService balanceService;

    @Before
    public void init() {
        balanceService = new BalanceService();
    }
//
//    @Test
//    public void getBalances_EmptyBalancesList_ReturnsEmptyList() {
//        List<BaseTransactionData> balances = balanceService.getBalances(new ArrayList<>());
//        Assert.assertTrue(balances.equals(new ArrayList<>()));
//    }
//
//    @Test
//    public void getBalances_StoreAndRetrieveBalances_ReturnsBalances() {
//        balanceService.addToBalance(new TransactionData(
//                new Hash("TransactionData 1".getBytes())));
//
//        List<BaseTransactionData> balances =
//                balanceService.getBalances(Arrays.asList(
//                        new Hash("Address1".getBytes()),
//                        new Hash("Address2".getBytes())));
//        List<BaseTransactionData> expectedBalances = Arrays.asList(
//                new BaseTransactionData(new Hash("Address1".getBytes()), 12.5),
//                new BaseTransactionData(new Hash("Address2".getBytes()), 44));
//        Assert.assertTrue(balances.equals(expectedBalances));
//    }
}
