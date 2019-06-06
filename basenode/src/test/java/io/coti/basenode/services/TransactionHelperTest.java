package io.coti.basenode.services;

import io.coti.basenode.crypto.DspConsensusCrypto;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.crypto.TransactionCrypto;
import io.coti.basenode.crypto.TransactionTrustScoreCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.ITrustScoreNodeValidatable;
import io.coti.basenode.model.AddressTransactionsHistories;
import io.coti.basenode.model.TransactionIndexes;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.LiveView.LiveViewService;
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
import testUtils.BaseNodeTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static testUtils.BaseNodeTestUtils.*;

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest(classes = {TransactionHelper.class})
@RunWith(SpringRunner.class)
@Slf4j
public class TransactionHelperTest {

    private static final String TRANSACTION_DESCRIPTION = "test";
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
            TransactionData transactionData1 = createRandomTransaction();
            TransactionData transactionData2 = createRandomTransaction();
            transactionHelper.startHandleTransaction(transactionData1);
            transactionHelper.startHandleTransaction(transactionData2);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }


    @Test
    public void endHandleTransaction_noExceptionIsThrown() {
        try {
            TransactionData transactionData1 = createRandomTransaction();
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
        baseTransactions.add(generateRandomInputBaseTransactionData(generateRandomHash(), -10));
        Assert.assertFalse(transactionHelper.validateBaseTransactionAmounts(baseTransactions));
    }

    @Test
    public void validateBaseTransactionsDataIntegrity_WhenAmountsEqual() {
        TransactionData transactionData = createRandomTransaction();
        Assert.assertTrue(transactionHelper.validateBaseTransactionsDataIntegrity(transactionData));
    }

    @Test
    public void validateBaseTransactionsDataIntegrity_WhenAmountsNotEqual() {
        TransactionData transactionData = createRandomTransaction();
        transactionData.getBaseTransactions().get(0).setAmount(new BigDecimal(-7));
        Assert.assertFalse(transactionHelper.validateBaseTransactionsDataIntegrity(transactionData));
    }

    @Test
    public void validateBaseTransactionsDataIntegrity_trustScoreVariations() {
        TransactionData transactionData = createRandomTransaction();
        Assert.assertTrue(transactionHelper.validateBaseTransactionsDataIntegrity(transactionData));

        BaseTransactionData baseTransactionData = generateRollingReserveData(generateRandomHash(), 0);
        List<BaseTransactionData> baseTransactions = transactionData.getBaseTransactions();
        List<TrustScoreNodeResultData> trustScoreNodeResults = new ArrayList<>();
        TrustScoreNodeResultData trustScoreNodeResultData = new TrustScoreNodeResultData(generateRandomHash(), false);
        trustScoreNodeResults.add(trustScoreNodeResultData);
        TrustScoreNodeResultData trustScoreNodeResultData2 = new TrustScoreNodeResultData(generateRandomHash(), true);
        trustScoreNodeResults.add(trustScoreNodeResultData2);

        // Expects 3 TrustScoreNodeResultData
        ((ITrustScoreNodeValidatable)baseTransactionData).setTrustScoreNodeResult(trustScoreNodeResults);
        baseTransactions.add(baseTransactionData);
        Assert.assertFalse(transactionHelper.validateBaseTransactionsDataIntegrity(transactionData));

        TrustScoreNodeResultData trustScoreNodeResultData3 = new TrustScoreNodeResultData(generateRandomHash(), false);
        trustScoreNodeResults.add(trustScoreNodeResultData3);

        // Expects 3 TrustScoreNodeResultData with at least 2 of them valid
        baseTransactions.add(baseTransactionData);
        Assert.assertFalse(transactionHelper.validateBaseTransactionsDataIntegrity(transactionData));

        // Expects 3 TrustScoreNodeResultData with at least 2 of them valid - actual 2 valid
        trustScoreNodeResultData3.setValid(true);
        Assert.assertTrue(transactionHelper.validateBaseTransactionsDataIntegrity(transactionData));

        // Expects 3 TrustScoreNodeResultData with at least 2 of them valid - actual 3 valid
        trustScoreNodeResultData.setValid(true);
        Assert.assertTrue(transactionHelper.validateBaseTransactionsDataIntegrity(transactionData));

        // Expects 3 TrustScoreNodeResultData with at least 2 of them valid - actual 4
        TrustScoreNodeResultData trustScoreNodeResultData4 = new TrustScoreNodeResultData(generateRandomHash(), false);
        trustScoreNodeResults.add(trustScoreNodeResultData4);
        Assert.assertFalse(transactionHelper.validateBaseTransactionsDataIntegrity(transactionData));
    }

    @Test
    public void updateAddressTransactionHistory_noException () {
        try {
            TransactionData transactionData = createRandomTransaction();
            List<BaseTransactionData> baseTransactions = generateValidateBaseTransactionData();
            transactionData.setBaseTransactions(baseTransactions);
            transactionHelper.updateAddressTransactionHistory(transactionData);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void updateAddressTransactionHistory_emptyMap_addingBaseTransactions () {

        TransactionData transactionData = createRandomTransaction();
        List<BaseTransactionData> baseTransactions = generateValidateBaseTransactionData();
        transactionData.setBaseTransactions(baseTransactions);
        Map<Hash, AddressTransactionsHistory> addressToTransactionsHistoryMap = new HashMap<>();
        transactionHelper.updateAddressTransactionHistory(addressToTransactionsHistoryMap, transactionData);
        baseTransactions.forEach( baseTransactionData -> {
            Assert.assertTrue(addressToTransactionsHistoryMap.containsKey(baseTransactionData.getAddressHash()));
        });
    }


    @Test
    public void validateBaseTransactionTrustScoreNodeResults_isValid() {
        TransactionData transactionData = createRandomTransaction();
        Assert.assertTrue(transactionHelper.validateBaseTransactionTrustScoreNodeResults(transactionData));
    }



    @Test
    public void validateTransactionType_isNotValid() {
        List<BaseTransactionData> baseTransactions = generateValidateBaseTransactionData();
        TransactionData TransactionData = new TransactionData(baseTransactions, generateRandomHash(), TRANSACTION_DESCRIPTION, generateRandomTrustScore(), Instant.now(), TransactionType.Transfer);
        Assert.assertFalse(transactionHelper.validateTransactionType(TransactionData)); // missing PaymentInputBaseTransactionData
    }

    private List<BaseTransactionData> generateValidateBaseTransactionData() {
        List<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(generateFullNodeFeeData(generateRandomHash(), 7));
        baseTransactions.add(generateNetworkFeeData(generateRandomHash(), 5, 1, 3));
        baseTransactions.add(generateRollingReserveData(generateRandomHash(), 4));
        baseTransactions.add(generateReceiverBaseTransactionData(generateRandomHash(), 3));
        baseTransactions.add(generateRandomInputBaseTransactionData(generateRandomHash(), -19));
        return baseTransactions;
    }

    //
    @Test
    public void validateBaseTransactionTrustScoreNodeResult_isValid() {
        NetworkFeeData networkFeeData = (NetworkFeeData) generateNetworkFeeData(generateRandomHash(), generateRandomPositiveAmount());
        networkFeeData.setNetworkFeeTrustScoreNodeResult(new ArrayList());
        for (int i = 0; i < TRUSTSCORE_NODE_RESULT_VALID_SIZE; i++) {
            networkFeeData.getNetworkFeeTrustScoreNodeResult().add(new TrustScoreNodeResultData(generateRandomHash(), true));
        }
        Assert.assertTrue(transactionHelper.validateBaseTransactionTrustScoreNodeResult(networkFeeData));
    }


    @Test
    public void validateBaseTransactionTrustScoreNodeResult_isNotValid() {
        NetworkFeeData networkFeeData1 = (NetworkFeeData) generateNetworkFeeData(generateRandomHash(), generateRandomPositiveAmount());
        networkFeeData1.setNetworkFeeTrustScoreNodeResult(new ArrayList());
        for (int i = 0; i < TRUSTSCORE_NODE_RESULT_VALID_SIZE; i++) {
            networkFeeData1.getNetworkFeeTrustScoreNodeResult().add(new TrustScoreNodeResultData(generateRandomHash(), false));
        }

        NetworkFeeData networkFeeData2 = (NetworkFeeData) generateNetworkFeeData(generateRandomHash(), generateRandomPositiveAmount());
        networkFeeData2.setNetworkFeeTrustScoreNodeResult(new ArrayList());
        for (int i = 0; i < TRUSTSCORE_NODE_RESULT_NOT_VALID_SIZE; i++) {
            networkFeeData2.getNetworkFeeTrustScoreNodeResult().add(new TrustScoreNodeResultData(generateRandomHash(), true));
        }

        Assert.assertTrue(!transactionHelper.validateBaseTransactionTrustScoreNodeResult(networkFeeData1) &&
                !transactionHelper.validateBaseTransactionTrustScoreNodeResult(networkFeeData2));
    }

    @Test
    public void isTransactionHashProcessing() {
        TransactionData transactionData = createRandomTransaction();
        transactionHelper.startHandleTransaction(transactionData);
        transactionHelper.isTransactionHashProcessing(transactionData.getHash());
    }

    @Test
    public void testSetTransactionStateToSaved_noExceptionIsThrown() {
        try {
            TransactionData transactionData = createRandomTransaction();
            transactionHelper.startHandleTransaction(transactionData);
            transactionHelper.setTransactionStateToSaved(transactionData);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void setTransactionStateToFinished_noExceptionIsThrown() {
        try {
            TransactionData transactionData = createRandomTransaction();
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
        return createRandomTransaction();
    }


    @Test
    public void getTotalTransactions() {
        long totalTransactionsBeforeIncrement = transactionHelper.getTotalTransactions();
        transactionHelper.incrementTotalTransactions();
        transactionHelper.incrementTotalTransactions();
        long totalTransactionsAfterIncrement = transactionHelper.getTotalTransactions();
        Assert.assertEquals(totalTransactionsBeforeIncrement + 2, totalTransactionsAfterIncrement);
    }

    @Test
    public void incrementTotalTransactions() {
        transactionHelper.incrementTotalTransactions();
        Assert.assertEquals(2, transactionHelper.incrementTotalTransactions());
    }

    @Test
    public void addNoneIndexedTransaction_noExceptionIsThrown() {
        try {
            TransactionData transactionData = createRandomTransaction();
            transactionHelper.addNoneIndexedTransaction(transactionData);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void removeNoneIndexedTransaction_noExceptionIsThrown() {
        try {
            TransactionData transactionData = createRandomTransaction();
            transactionHelper.addNoneIndexedTransaction(transactionData);
            transactionHelper.removeNoneIndexedTransaction(transactionData);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }


}
