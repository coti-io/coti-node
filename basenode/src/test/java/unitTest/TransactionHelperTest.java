package unitTest;

import io.coti.basenode.crypto.DspConsensusCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.crypto.TransactionTrustScoreCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.model.AddressTransactionsHistories;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.LiveView.LiveViewService;
import io.coti.basenode.services.TransactionHelper;
import io.coti.basenode.services.TransactionIndexService;
import io.coti.basenode.services.interfaces.IBalanceService;
import io.coti.basenode.services.interfaces.IClusterService;
import io.coti.basenode.services.interfaces.IConfirmationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static testUtils.TestUtils.*;

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
@Slf4j
public class TransactionHelperTest {

    public static final String TRANSACTION_DESCRIPTION = "test";
    private static final int SIZE_OF_HASH = 64;
    private static final int TRUSTSCORE_NODE_RESULT_VALID_SIZE = 3;
    private static final int TRUSTSCORE_NODE_RESULT_NOT_VALID_SIZE = 4;

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

    @Before
    public void init(){
        log.info("Starting  - " + this.getClass().getSimpleName());
    }

    @Test
    public void testStartHandleTransaction_noExceptionIsThrown() {
        try {
            TransactionData transactionData1 = TestUtils.generateRandomTransaction();
            TransactionData transactionData2 = TestUtils.generateRandomTransaction();
            transactionHelper.startHandleTransaction(transactionData1);
            transactionHelper.startHandleTransaction(transactionData2);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testEndHandleTransaction_noExceptionIsThrown() {
        try {
            TransactionData transactionData1 = TestUtils.generateRandomTransaction();
            transactionHelper.startHandleTransaction(transactionData1);
            transactionHelper.endHandleTransaction(transactionData1);
            transactionHelper.endHandleTransaction(transactionData1);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testValidateBaseTransactionAmounts_WhenAmountsEqual() {
        List<BaseTransactionData> baseTransactions = generateValidateBaseTransactionData();
        Assert.assertTrue(transactionHelper.validateBaseTransactionAmounts(baseTransactions));
    }


    @Test
    public void testValidateBaseTransactionAmounts_WhenAmountsNotEqual() {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(generateFullNodeFeeData(generateRandomHash(SIZE_OF_HASH), 6));
        baseTransactions.add(generateFullNodeFeeData(generateRandomHash(SIZE_OF_HASH), 5));
        baseTransactions.add(createInputBaseTransactionDataWithSpecificHashAndCount(generateRandomHash(SIZE_OF_HASH), -10));
        Assert.assertFalse(transactionHelper.validateBaseTransactionAmounts(baseTransactions));
    }

    @Test
    public void testValidateTransactionType_isValid() {
        List<BaseTransactionData> baseTransactions = generateValidateBaseTransactionData();
        TransactionData TransactionData = new TransactionData(baseTransactions, generateRandomHash(SIZE_OF_HASH), TRANSACTION_DESCRIPTION, generateRandomTrustScore(), new Date(), TransactionType.Payment);
        Assert.assertTrue(transactionHelper.validateTransactionType(TransactionData));
    }


    @Test
    public void testValidateTransactionType_isNotValid() {
        List<BaseTransactionData> baseTransactions = generateValidateBaseTransactionData();
        TransactionData TransactionData = new TransactionData(baseTransactions, generateRandomHash(SIZE_OF_HASH), TRANSACTION_DESCRIPTION, generateRandomTrustScore(), new Date(), TransactionType.Transfer);
        Assert.assertFalse(transactionHelper.validateTransactionType(TransactionData));
    }

    private List<BaseTransactionData> generateValidateBaseTransactionData() {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(generateFullNodeFeeData(generateRandomHash(SIZE_OF_HASH), 7));
        baseTransactions.add(generateNetworkFeeData(generateRandomHash(SIZE_OF_HASH), 5));
        baseTransactions.add(generateRollingReserveData(generateRandomHash(SIZE_OF_HASH), 4));
        baseTransactions.add(generateReceiverBaseTransactionData(generateRandomHash(SIZE_OF_HASH), 3));
        baseTransactions.add(createInputBaseTransactionDataWithSpecificHashAndCount(generateRandomHash(SIZE_OF_HASH), -19));
        return baseTransactions;
    }

    //
    @Test
    public void testValidateBaseTransactionTrustScoreNodeResult_isValid() {
        NetworkFeeData networkFeeData = (NetworkFeeData) generateNetworkFeeData(generateRandomHash(SIZE_OF_HASH), generateRandomCount());
        networkFeeData.setNetworkFeeTrustScoreNodeResult(new ArrayList());
        for (int i = 0; i < TRUSTSCORE_NODE_RESULT_VALID_SIZE; i++) {
            networkFeeData.getNetworkFeeTrustScoreNodeResult().add(new TrustScoreNodeResultData(generateRandomHash(SIZE_OF_HASH), true));
        }
        Assert.assertTrue(transactionHelper.validateBaseTransactionTrustScoreNodeResult(networkFeeData));
    }


    @Test
    public void testValidateBaseTransactionTrustScoreNodeResult_isNotValid() {
        NetworkFeeData networkFeeData1 = (NetworkFeeData) generateNetworkFeeData(generateRandomHash(SIZE_OF_HASH), generateRandomCount());
        networkFeeData1.setNetworkFeeTrustScoreNodeResult(new ArrayList());
        for (int i = 0; i < TRUSTSCORE_NODE_RESULT_VALID_SIZE; i++) {
            networkFeeData1.getNetworkFeeTrustScoreNodeResult().add(new TrustScoreNodeResultData(generateRandomHash(SIZE_OF_HASH), false));
        }

        NetworkFeeData networkFeeData2 = (NetworkFeeData) generateNetworkFeeData(generateRandomHash(SIZE_OF_HASH), generateRandomCount());
        networkFeeData2.setNetworkFeeTrustScoreNodeResult(new ArrayList());
        for (int i = 0; i < TRUSTSCORE_NODE_RESULT_NOT_VALID_SIZE; i++) {
            networkFeeData2.getNetworkFeeTrustScoreNodeResult().add(new TrustScoreNodeResultData(generateRandomHash(SIZE_OF_HASH), true));
        }

        Assert.assertTrue(!transactionHelper.validateBaseTransactionTrustScoreNodeResult(networkFeeData1) &&
                !transactionHelper.validateBaseTransactionTrustScoreNodeResult(networkFeeData2));
    }

    @Test
    public void testIsTransactionHashProcessing() {
        TransactionData transactionData = TestUtils.generateRandomTransaction();
        transactionHelper.startHandleTransaction(transactionData);
        transactionHelper.isTransactionHashProcessing(transactionData.getHash());
    }

    @Test
    public void testSetTransactionStateToSaved_noExceptionIsThrown() {
        try {
            TransactionData transactionData = TestUtils.generateRandomTransaction();
            transactionHelper.startHandleTransaction(transactionData);
            transactionHelper.setTransactionStateToSaved(transactionData);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testSetTransactionStateToFinished_noExceptionIsThrown() {
        try {
            TransactionData transactionData = TestUtils.generateRandomTransaction();
            transactionHelper.startHandleTransaction(transactionData);
            transactionHelper.setTransactionStateToFinished(transactionData);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void isConfirmed_whenTccConfirmedAndNotDspConfirmed_returnsFalse() {
        TransactionData tx = createTransaction();
        tx.setTrustChainConsensus(true);
        tx.setDspConsensusResult(new DspConsensusResult(generateRandomHash(SIZE_OF_HASH)));
        tx.getDspConsensusResult().setDspConsensus(false);
        Assert.assertFalse(transactionHelper.isConfirmed(tx));
    }

    @Test
    public void isConfirmed_whenTccNotConfirmedAndDspConfirmed_returnsFalse() {
        TransactionData tx = createTransaction();
        tx.setTrustChainConsensus(false);
        tx.setDspConsensusResult(new DspConsensusResult(generateRandomHash(SIZE_OF_HASH)));
        tx.getDspConsensusResult().setDspConsensus(true);
        Assert.assertFalse(transactionHelper.isConfirmed(tx));
    }

    private TransactionData createTransaction() {
        return TestUtils.generateRandomTransaction();
    }


    @Test
    public void testGetTotalTransactions() {
        long totalTransactionsBeforeIncrement = transactionHelper.getTotalTransactions();
        transactionHelper.incrementTotalTransactions();
        transactionHelper.incrementTotalTransactions();
        long totalTransactionsAfterIncrement = transactionHelper.getTotalTransactions();
        Assert.assertTrue(totalTransactionsBeforeIncrement + 2 ==
                totalTransactionsAfterIncrement);
    }

    @Test
    public void testIncrementTotalTransactions() {
        transactionHelper.incrementTotalTransactions();
        Assert.assertTrue(transactionHelper.incrementTotalTransactions() == 2);
    }

    @Test
    public void testAddNoneIndexedTransaction_noExceptionIsThrown() {
        try {
            TransactionData transactionData = TestUtils.generateRandomTransaction();
            transactionHelper.addNoneIndexedTransaction(transactionData);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testRemoveNoneIndexedTransaction_noExceptionIsThrown() {
        try {
            TransactionData transactionData = TestUtils.generateRandomTransaction();
            transactionHelper.addNoneIndexedTransaction(transactionData);
            transactionHelper.removeNoneIndexedTransaction(transactionData);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
