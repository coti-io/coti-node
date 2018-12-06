package io.coti.trustscore;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.trustscore.data.Buckets.BucketTransactionEventsData;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.TransactionEventData;
import io.coti.trustscore.model.BucketTransactionEvents;
import io.coti.trustscore.services.BucketTransactionService;
import io.coti.trustscore.services.calculationServices.BucketTransactionsCalculator;
import javafx.util.Pair;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static io.coti.trustscore.BucketUtil.generateRulesDataObject;
import static io.coti.trustscore.utils.DatesCalculation.*;
import static io.coti.trustscore.utils.MathCalculation.ifTwoNumbersAreEqualOrAlmostEqual;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {BucketTransactionService.class,
        BucketTransactionsCalculator.class,
        BaseNodeRocksDBConnector.class,
        BucketTransactionService.class,
        BucketTransactionEvents.class
})
public class BucketTransactionServiceTest {

    @Autowired
    private BucketTransactionService bucketTransactionService;


    private BucketTransactionEventsData bucketTransactionEventsData;

 /*   @Before
    public void setUp() {
        bucketTransactionService.init(generateRulesDataObject());
        bucketTransactionEventsData = new BucketTransactionEventsData();
        bucketTransactionEventsData.setUserType(UserType.WALLET);

        TransactionData transactionData = BucketUtil.createTransactionWithSpecificHash(new Hash("1234"), new Hash("abcd"), 70.45);
        transactionData.getBaseTransactions().get(0).setAmount(new BigDecimal(8));

        TransactionData transactionData2 = BucketUtil.createTransactionWithSpecificHash(new Hash("2345"), new Hash("abcd"), 70.45);
        transactionData2.getBaseTransactions().get(0).setAmount(new BigDecimal(5));

        bucketTransactionService.addEventToCalculations(new TransactionEventData(transactionData), bucketTransactionEventsData);
        bucketTransactionService.addEventToCalculations(new TransactionEventData(transactionData2), bucketTransactionEventsData);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void addEventToCalculationsTest() {
        //BucketTransactionService bucketTransactionService2 =  bucketTransactionService;
        Assert.assertTrue(
                (ifTwoNumbersAreEqualOrAlmostEqual(bucketTransactionEventsData.getCurrentMonthBalance()
                        .get(setDateOnBeginningOfDay(decreaseTodayDateByDays(0)).getTime()).getValue(), 0.03568973444951873))
                        && (ifTwoNumbersAreEqualOrAlmostEqual(bucketTransactionEventsData.getCurrentDateNumberOfTransactionsContribution(), 0.0))
                        && (ifTwoNumbersAreEqualOrAlmostEqual(bucketTransactionEventsData.getCurrentDateTurnOverContribution(), 0.0))
        );
    }

    @Test
    public void getBucketSumScoreTest() {
        double sumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsData);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore, 0.02676730083));
    }

    @Test
    public void BucketTransactionService_simulationOfZeroDayDecayedTest() {
        decayDays(0);

        double sumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsData);

        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore, 0.02676730083));
    }

    @Test
    public void BucketTransactionService_simulationOfDayDecayedTest() {
        decayDays(1);

        double sumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsData);

        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore, 0.05292323566));
    }

    @Test
    public void BucketTransactionService_simulationOfTwoDayDecayedTest() {
        decayDays(2);

        double sumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsData);

        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore, 0.07848176812));
    }

    @Test
    public void BucketTransactionService_simulationOfThreeDayDecayed() {
        decayDays(3);

        double sumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsData);

        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore, 0.1034565429));
    }

    @Test
    public void BucketTransactionService_simulationOfThreeDayDecayedAndAddingNewTransactionTest() {
        decayDays(3);
        TransactionData transactionData = BucketUtil.createTransactionWithSpecificHash(new Hash("1236"), new Hash("abcd"), 70.45);
        transactionData.getBaseTransactions().get(0).setAmount(new BigDecimal(7));
        bucketTransactionService.addEventToCalculations(new TransactionEventData(transactionData), bucketTransactionEventsData);

        double sumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsData);

        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore, 0.1178458163));
    }

    @Test
    public void BucketTransactionService_complicatedScenarioTest() {
        // Decay 3 days, and adding transaction of 7 coti
        decayDays(3);
        TransactionData transactionData = BucketUtil.createTransactionWithSpecificHash(new Hash("1236"), new Hash("abcd"), 70.45);
        transactionData.getBaseTransactions().get(0).setAmount(new BigDecimal(7));
        bucketTransactionService.addEventToCalculations(new TransactionEventData(transactionData), bucketTransactionEventsData);
        double sumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsData);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore, 0.1178458163));

        // Decay 2 days
        decayDays(2);
        double sumScore1 = bucketTransactionService.getBucketSumScore(bucketTransactionEventsData);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore1, 0.1938972193));

        // Decay 2 days
        decayDays(26);
        double sumScore26 = bucketTransactionService.getBucketSumScore(bucketTransactionEventsData);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore26, 0.9200733628));

        // Adding transaction of 11 coti
        TransactionData transactionData2 = BucketUtil.createTransactionWithSpecificHash(new Hash("3456"), new Hash("abcd"), 70.45);
        transactionData2.getBaseTransactions().get(0).setAmount(new BigDecimal(11));
        bucketTransactionService.addEventToCalculations(new TransactionEventData(transactionData2), bucketTransactionEventsData);
        double sumScore2 = bucketTransactionService.getBucketSumScore(bucketTransactionEventsData);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore2, 0.9426197695));

        // Adding transaction of -3 coti
        TransactionData transactionData3 = BucketUtil.createTransactionWithSpecificHash(new Hash("3457"), new Hash("abcd"), 70.45);
        transactionData3.getBaseTransactions().get(0).setAmount(new BigDecimal(-3));
        bucketTransactionService.addEventToCalculations(new TransactionEventData(transactionData3), bucketTransactionEventsData);
        double sumScore3 = bucketTransactionService.getBucketSumScore(bucketTransactionEventsData);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore3, 0.9391583623));

    }

    public void decayDays(int numOfDays) {
        // simulation of moving to a new day
        bucketTransactionEventsData.setLastUpdate(decreaseTodayDateByDays(numOfDays));
        HashMap<Long, Pair<Double, Double>> CurrentMonthBalanceWithDateChangedEntry = new HashMap<>();
        for (Map.Entry<Long, Pair<Double, Double>> currentMonthBalanceEntry : bucketTransactionEventsData.getCurrentMonthBalance().entrySet()) {
            long dateBeforeDEcayed = currentMonthBalanceEntry.getKey();
            long oldTime = addToDateByDays(dateBeforeDEcayed, -numOfDays).getTime();
            Pair<Double, Double> value = currentMonthBalanceEntry.getValue();
            CurrentMonthBalanceWithDateChangedEntry.put(oldTime, value);
        }
        bucketTransactionEventsData.setCurrentMonthBalance(CurrentMonthBalanceWithDateChangedEntry);
    } */
}