package unitTest;

import io.coti.common.data.*;
import io.coti.common.http.BaseResponse;
import io.coti.common.http.GetTransactionResponse;
import io.coti.common.model.AddressesTransactionsHistory;
import io.coti.common.model.TransactionIndexes;
import io.coti.common.model.Transactions;
import io.coti.common.services.TransactionHelper;
import io.coti.common.services.TransactionIndexService;
import io.coti.common.services.interfaces.IBalanceService;
import io.coti.common.services.interfaces.IClusterService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;
import unitTest.crypto.DspConsensusCrypto;
import unitTest.crypto.TransactionTrustScoreCrypto;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

import static org.mockito.Mockito.when;

@TestPropertySource(locations = "../test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {TransactionHelper.class,
                AddressesTransactionsHistory.class,
                IBalanceService.class,
                IClusterService.class,
                Transactions.class,
                DspConsensusCrypto.class,
                TransactionTrustScoreCrypto.class,
                TransactionIndexService.class,
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
    private TransactionIndexService transactionIndexService;
    @MockBean
    private TransactionIndexes transactionIndexes;

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

        Assert.assertTrue(transactionHelper.isLegalBalance(Arrays.asList(baseTransactionData1, baseTransactionData2, baseTransactionData3)));
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

        Assert.assertFalse(transactionHelper.isLegalBalance(Arrays.asList(baseTransactionData1, baseTransactionData2, baseTransactionData3)));
    }

    @Test
    public void testStartHandleTransaction() {
        TransactionData transactionData1 = TestUtils.createTransactionWithSpecificHash(new Hash("AA"));
        TransactionData transactionData2 = TestUtils.createTransactionWithSpecificHash(new Hash("BB"));
        boolean handlingNewTransaction1 = transactionHelper.startHandleTransaction(transactionData1);
        boolean failedHandlingExistTransaction1 = transactionHelper.startHandleTransaction(transactionData1);
        boolean handlingNewTransaction2 = transactionHelper.startHandleTransaction(transactionData2);
        Assert.assertTrue(handlingNewTransaction1
                && !failedHandlingExistTransaction1
                && handlingNewTransaction2);
    }

    @Test
    public void testEndHandleTransaction() {
        TransactionData transactionData1 = TestUtils.createTransactionWithSpecificHash(new Hash("CC"));
        transactionHelper.startHandleTransaction(transactionData1);
        transactionHelper.endHandleTransaction(transactionData1);
        boolean successInsertToMapAfterDeleted = transactionHelper.startHandleTransaction(transactionData1);
        Assert.assertTrue(successInsertToMapAfterDeleted);
    }

    @Test
    public void testGetTransactionDetails() {
        TransactionData tx = createTransaction();
        when(transactions.getByHash(tx.getHash())).thenReturn(tx);
        ResponseEntity<BaseResponse> transactionDetails =
                transactionHelper.getTransactionDetails(tx.getHash());
        String s = transactionDetails.toString();
        Assert.assertTrue(tx.getHash().toHexString()
                .equals(((GetTransactionResponse) transactionDetails.getBody()).getTransactionData().getHash()));
    }

    @Test
    public void isConfirmed_whenTccConfirmedAndDspConfirmed_returnsTrue() {
        TransactionData tx = createTransaction();
        tx.setTrustChainConsensus(true);
        tx.setDspConsensusResult(new DspConsensusResult(new Hash("55")));
        tx.getDspConsensusResult().setDspConsensus(true);
        Assert.assertTrue(transactionHelper.isConfirmed(tx));
    }

    @Test
    public void isConfirmed_whenTccConfirmedAndNotDspConfirmed_returnsFalse() {
        TransactionData tx = createTransaction();
        tx.setTrustChainConsensus(true);
        tx.setDspConsensusResult(new DspConsensusResult(new Hash("66")));
        tx.getDspConsensusResult().setDspConsensus(false);
        Assert.assertFalse(transactionHelper.isConfirmed(tx));
    }

    @Test
    public void isConfirmed_whenTccNotConfirmedAndDspConfirmed_returnsFalse() {
        TransactionData tx = createTransaction();
        tx.setTrustChainConsensus(false);
        tx.setDspConsensusResult(new DspConsensusResult(new Hash("77")));
        tx.getDspConsensusResult().setDspConsensus(true);
        Assert.assertFalse(transactionHelper.isConfirmed(tx));
    }

    private TransactionData createTransaction() {
        return TestUtils.createTransactionWithSpecificHash(new Hash("AE"));
    }


    @Test
    public void testIncrementTotalTransactions() {
        long totalTransactionsBeforeIncrement = transactionHelper.getTotalTransactions();
        transactionHelper.incrementTotalTransactions();
        transactionHelper.incrementTotalTransactions();
        long totalTransactionsAfterIncrement = transactionHelper.getTotalTransactions();
        Assert.assertTrue(totalTransactionsBeforeIncrement + 2 ==
                totalTransactionsAfterIncrement);
    }
}
