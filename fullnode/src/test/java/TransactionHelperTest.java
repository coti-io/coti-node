import io.coti.common.crypto.DspConsensusCrypto;
import io.coti.common.crypto.TransactionTrustScoreCrypto;
import io.coti.common.data.BaseTransactionData;
import io.coti.common.data.Hash;
import io.coti.common.data.SignatureData;
import io.coti.common.model.AddressesTransactionsHistory;
import io.coti.common.model.Transactions;
import io.coti.common.services.TransactionHelper;
import io.coti.common.services.interfaces.IBalanceService;
import io.coti.common.services.interfaces.IClusterService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {TransactionHelper.class,
                AddressesTransactionsHistory.class,
                IBalanceService.class,
                IClusterService.class,
                Transactions.class,
                DspConsensusCrypto.class,
                TransactionTrustScoreCrypto.class
        }
)
public class TransactionHelperTest {

    @Autowired
    private TransactionHelper transactionHelper;

    @MockBean
    private AddressesTransactionsHistory addressesTransactionsHistory;
    @MockBean
    private IBalanceService balanceService;
    @MockBean
    private IClusterService clusterService;
    @MockBean
    private Transactions transactions;
    @MockBean
    private DspConsensusCrypto dspConsensusCrypto;
    @MockBean
    private TransactionTrustScoreCrypto transactionTrustScoreCrypto;

    @Test
    public void isLegalBalance_whenBalanceIsLegal() {
        BaseTransactionData baseTransactionData1 = new BaseTransactionData(new Hash("caba14b7fe219b3da5dee0c29389c88e4d134333a2ee104152d6e9f7b673be9e0e28ca511d1ac749f46bea7f1ab25818f335ab9111a6c5eebe2f650974e12d1b7dccd4d7"),
                new BigDecimal(-2000000),
                new Hash("AE"),
                new SignatureData("", ""),
                new Date());

        BaseTransactionData baseTransactionData2 = new BaseTransactionData(new Hash("caba14b7fe219b3da5dee0c29389c88e4d134333a2ee104152d6e9f7b673be9e0e28ca511d1ac749f46bea7f1ab25818f335ab9111a6c5eebe2f650974e12d1b7dccd4d7"),
                new BigDecimal(4000000),
                new Hash("AE"),
                new SignatureData("", ""),
                new Date());

        BaseTransactionData baseTransactionData3 = new BaseTransactionData(new Hash("caba14b7fe219b3da5dee0c29389c88e4d134333a2ee104152d6e9f7b673be9e0e28ca511d1ac749f46bea7f1ab25818f335ab9111a6c5eebe2f650974e12d1b7dccd4d7"),
                new BigDecimal(-2000000),
                new Hash("AE"),
                new SignatureData("", ""),
                new Date());

        Assert.assertTrue(transactionHelper.isLegalBalance(Arrays.asList(baseTransactionData1 ,baseTransactionData2, baseTransactionData3)));
    }

    @Test
    public void isLegalBalance_whenBalanceIsNotLegal() {
        BaseTransactionData baseTransactionData1 = new BaseTransactionData(new Hash("caba14b7fe219b3da5dee0c29389c88e4d134333a2ee104152d6e9f7b673be9e0e28ca511d1ac749f46bea7f1ab25818f335ab9111a6c5eebe2f650974e12d1b7dccd4d7"),
                new BigDecimal(-2000000),
                new Hash("AE"),
                new SignatureData("", ""),
                new Date());

        BaseTransactionData baseTransactionData2 = new BaseTransactionData(new Hash("caba14b7fe219b3da5dee0c29389c88e4d134333a2ee104152d6e9f7b673be9e0e28ca511d1ac749f46bea7f1ab25818f335ab9111a6c5eebe2f650974e12d1b7dccd4d7"),
                new BigDecimal(5000000),
                new Hash("AE"),
                new SignatureData("", ""),
                new Date());

        BaseTransactionData baseTransactionData3 = new BaseTransactionData(new Hash("caba14b7fe219b3da5dee0c29389c88e4d134333a2ee104152d6e9f7b673be9e0e28ca511d1ac749f46bea7f1ab25818f335ab9111a6c5eebe2f650974e12d1b7dccd4d7"),
                new BigDecimal(-2000000),
                new Hash("AE"),
                new SignatureData("", ""),
                new Date());

        Assert.assertFalse(transactionHelper.isLegalBalance(Arrays.asList(baseTransactionData1 ,baseTransactionData2, baseTransactionData3)));
    }
}
