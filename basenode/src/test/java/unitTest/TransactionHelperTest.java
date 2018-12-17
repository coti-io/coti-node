package unitTest;

import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.http.BaseResponse;
import io.coti.basenode.model.AddressTransactionsHistories;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.LiveView.LiveViewService;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IClusterService;
import io.coti.basenode.services.interfaces.IConfirmationService;
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
import io.coti.basenode.crypto.DspConsensusCrypto;
import io.coti.basenode.crypto.TransactionTrustScoreCrypto;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

import static org.mockito.Mockito.when;

@TestPropertySource(locations = "../test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {TransactionHelper.class,
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
    private IBalanceService balanceService;
    @MockBean
    private IClusterService clusterService;
    @MockBean
    private Transactions transactions;
    @MockBean
    private TransactionIndexService transactionIndexService;
    @MockBean
    private TransactionIndexes transactionIndexes;
    @MockBean
    private AddressTransactionsHistories addressTransactionsHistories;
    @MockBean
    private TransactionCrypto transactionCrypto;
    @MockBean
    private IConfirmationService confirmationService;
    @MockBean
    private DspConsensusCrypto dspConsensusCrypto;
    @MockBean
    private TransactionTrustScoreCrypto transactionTrustScoreCrypto;
    @MockBean
    private NodeCryptoHelper nodeCryptoHelper;
    @MockBean
    private LiveViewService LiveViewService;

    @Test
    public void testStartHandleTransaction() {
        TransactionData transactionData1 = TestUtils.createTransactionWithSpecificHash(new Hash("AA"));
        TransactionData transactionData2 = TestUtils.createTransactionWithSpecificHash(new Hash("BB"));
        transactionHelper.startHandleTransaction(transactionData1);
        transactionHelper.startHandleTransaction(transactionData2);
    }

    @Test
    public void testEndHandleTransaction() {
        TransactionData transactionData1 = TestUtils.createTransactionWithSpecificHash(new Hash("CC"));
        transactionHelper.startHandleTransaction(transactionData1);
        transactionHelper.endHandleTransaction(transactionData1);transactionHelper.endHandleTransaction(transactionData1);
    }


    // no index
//    @Test
//    public void isConfirmed_whenTccConfirmedAndDspConfirmed_returnsTrue() {
//        TransactionData tx = createTransaction();
//        tx.setTrustChainConsensus(true);
//        tx.setDspConsensusResult(new DspConsensusResult(new Hash("55")));
//        tx.getDspConsensusResult().setDspConsensus(true);
//        Assert.assertTrue(transactionHelper.isConfirmed(tx));
//    }

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
