package io.coti.basenode.services;

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

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest(classes = {TransactionHelper.class})
@RunWith(SpringRunner.class)
@Slf4j
public class TransactionHelperTest {

    public static final String TRANSACTION_DESCRIPTION = "test";
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
    public void setUp() {
        log.info("Starting  - " + this.getClass().getSimpleName());
    }

    @Test
    public void startHandleTransaction_noExceptionIsThrown() {
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
    public void endHandleTransaction_noExceptionIsThrown() {
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
    public void validateBaseTransactionAmounts_WhenAmountsEqual() {
        List<BaseTransactionData> baseTransactions = generateValidateBaseTransactionData();
        Assert.assertTrue(transactionHelper.validateBaseTransactionAmounts(baseTransactions));
    }


    @Test
    public void validateBaseTransactionAmounts_WhenAmountsNotEqual() {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(generateFullNodeFeeData(generateRandomHash(), 6));
        baseTransactions.add(generateFullNodeFeeData(generateRandomHash(), 5));
        baseTransactions.add(createInputBaseTransactionDataWithSpecificHashAndCount(generateRandomHash(), -10));
        Assert.assertFalse(transactionHelper.validateBaseTransactionAmounts(baseTransactions));
    }

    @Test
    public void validateTransactionType_isValid() {
        List<BaseTransactionData> baseTransactions = generateValidateBaseTransactionData();
        TransactionData TransactionData = new TransactionData(baseTransactions, generateRandomHash(), TRANSACTION_DESCRIPTION, generateRandomTrustScore(), new Date(), TransactionType.Payment);
        Assert.assertTrue(transactionHelper.validateTransactionType(TransactionData));
    }


    @Test
    public void validateTransactionType_isNotValid() {
        List<BaseTransactionData> baseTransactions = generateValidateBaseTransactionData();
        TransactionData TransactionData = new TransactionData(baseTransactions, generateRandomHash(), TRANSACTION_DESCRIPTION, generateRandomTrustScore(), new Date(), TransactionType.Transfer);
        Assert.assertFalse(transactionHelper.validateTransactionType(TransactionData));
    }

    private List<BaseTransactionData> generateValidateBaseTransactionData() {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(generateFullNodeFeeData(generateRandomHash(), 7));
        baseTransactions.add(generateNetworkFeeData(generateRandomHash(), 5));
        baseTransactions.add(generateRollingReserveData(generateRandomHash(), 4));
        baseTransactions.add(generateReceiverBaseTransactionData(generateRandomHash(), 3));
        baseTransactions.add(createInputBaseTransactionDataWithSpecificHashAndCount(generateRandomHash(), -19));
        return baseTransactions;
    }

    //
    @Test
    public void validateBaseTransactionTrustScoreNodeResult_isValid() {
        NetworkFeeData networkFeeData = (NetworkFeeData) generateNetworkFeeData(generateRandomHash(), generateRandomCount());
        networkFeeData.setNetworkFeeTrustScoreNodeResult(new ArrayList());
        for (int i = 0; i < TRUSTSCORE_NODE_RESULT_VALID_SIZE; i++) {
            networkFeeData.getNetworkFeeTrustScoreNodeResult().add(new TrustScoreNodeResultData(generateRandomHash(), true));
        }
        Assert.assertTrue(transactionHelper.validateBaseTransactionTrustScoreNodeResult(networkFeeData));
    }


    @Test
    public void validateBaseTransactionTrustScoreNodeResult_isNotValid() {
        NetworkFeeData networkFeeData1 = (NetworkFeeData) generateNetworkFeeData(generateRandomHash(), generateRandomCount());
        networkFeeData1.setNetworkFeeTrustScoreNodeResult(new ArrayList());
        for (int i = 0; i < TRUSTSCORE_NODE_RESULT_VALID_SIZE; i++) {
            networkFeeData1.getNetworkFeeTrustScoreNodeResult().add(new TrustScoreNodeResultData(generateRandomHash(), false));
        }

        NetworkFeeData networkFeeData2 = (NetworkFeeData) generateNetworkFeeData(generateRandomHash(), generateRandomCount());
        networkFeeData2.setNetworkFeeTrustScoreNodeResult(new ArrayList());
        for (int i = 0; i < TRUSTSCORE_NODE_RESULT_NOT_VALID_SIZE; i++) {
            networkFeeData2.getNetworkFeeTrustScoreNodeResult().add(new TrustScoreNodeResultData(generateRandomHash(), true));
        }

        Assert.assertTrue(!transactionHelper.validateBaseTransactionTrustScoreNodeResult(networkFeeData1) &&
                !transactionHelper.validateBaseTransactionTrustScoreNodeResult(networkFeeData2));
    }

    @Test
    public void isTransactionHashProcessing() {
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
    public void setTransactionStateToFinished_noExceptionIsThrown() {
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
        tx.setDspConsensusResult(new DspConsensusResult(generateRandomHash()));
        tx.getDspConsensusResult().setDspConsensus(false);
        Assert.assertFalse(transactionHelper.isConfirmed(tx));
    }

    @Test
    public void isConfirmed_whenTccNotConfirmedAndDspConfirmed_returnsFalse() {
        TransactionData tx = createTransaction();
        tx.setTrustChainConsensus(false);
        tx.setDspConsensusResult(new DspConsensusResult(generateRandomHash()));
        tx.getDspConsensusResult().setDspConsensus(true);
        Assert.assertFalse(transactionHelper.isConfirmed(tx));
    }

    private TransactionData createTransaction() {
        return TestUtils.generateRandomTransaction();
    }


    @Test
    public void getTotalTransactions() {
        long totalTransactionsBeforeIncrement = transactionHelper.getTotalTransactions();
        transactionHelper.incrementTotalTransactions();
        transactionHelper.incrementTotalTransactions();
        long totalTransactionsAfterIncrement = transactionHelper.getTotalTransactions();
        Assert.assertTrue(totalTransactionsBeforeIncrement + 2 ==
                totalTransactionsAfterIncrement);
    }

    @Test
    public void incrementTotalTransactions() {
        transactionHelper.incrementTotalTransactions();
        Assert.assertTrue(transactionHelper.incrementTotalTransactions() == 2);
    }

    @Test
    public void addNoneIndexedTransaction_noExceptionIsThrown() {
        try {
            TransactionData transactionData = TestUtils.generateRandomTransaction();
            transactionHelper.addNoneIndexedTransaction(transactionData);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void removeNoneIndexedTransaction_noExceptionIsThrown() {
        try {
            TransactionData transactionData = TestUtils.generateRandomTransaction();
            transactionHelper.addNoneIndexedTransaction(transactionData);
            transactionHelper.removeNoneIndexedTransaction(transactionData);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
