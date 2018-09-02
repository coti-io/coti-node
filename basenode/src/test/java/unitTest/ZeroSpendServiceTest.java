package unitTest;

import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.ZeroSpendService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ZeroSpendService.class)
public class ZeroSpendServiceTest {

    @Autowired
    private ZeroSpendService zeroSpendService;

    @Test
    public void getZeroSpendTransaction_whenTrustScoreEquals64_returnsZeroSpendingWithTrustScore64() {
        TransactionData transactionData = zeroSpendService.getZeroSpendTransaction(64.0);
        Assert.assertTrue(transactionData.getSenderTrustScore() == 64.0);
    }

    @Test
    public void getGenesisTransactions_return11ZeroSpending() {
        List<TransactionData> transactions = zeroSpendService.getGenesisTransactions();
        Assert.assertTrue(transactions.size() == 11);
    }
}
