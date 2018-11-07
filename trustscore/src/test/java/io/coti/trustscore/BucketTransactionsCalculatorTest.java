package io.coti.trustscore;

import io.coti.basenode.database.RocksDBConnector;
import io.coti.trustscore.services.calculationServices.BucketTransactionsCalculator;
import io.coti.trustscore.data.Buckets.BucketTransactionEventsData;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.BalanceCountAndContribution;
import io.coti.trustscore.config.rules.RulesData;
import io.coti.trustscore.config.rules.TransactionEventsScore;
import io.coti.trustscore.services.BucketTransactionService;
import io.coti.trustscore.services.calculationServices.BucketTransactionsCalculator;
import io.coti.trustscore.util.BucketUtil;
import io.coti.trustscore.utils.DatesCalculation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import static io.coti.trustscore.utils.MathCalculation.ifTwoNumbersAreEqualOrAlmostEqual;

@ContextConfiguration(classes = {
        BucketTransactionsCalculator.class,
        RocksDBConnector.class,
        BucketTransactionService.class,
})
@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
public class BucketTransactionsCalculatorTest {

    @Before
    public void setUp() {
        RulesData rulesData = BucketUtil.generateRulesDataObject();
        BucketTransactionsCalculator.init(rulesData);
    }


    @Test
    public void setBucketTransactionEventsDataTest() {
        BucketTransactionEventsData bucketTransactionEventsData = new BucketTransactionEventsData();
        bucketTransactionEventsData.setUserType(UserType.WALLET);
        BucketTransactionsCalculator bucketTransactionsCalculator = new BucketTransactionsCalculator(bucketTransactionEventsData);

        TransactionEventsScore transactionEventsScore = bucketTransactionsCalculator.getTransactionEventsScore();
        Assert.assertTrue(transactionEventsScore.getTransactionEventScoreList().size() == 3);
    }

    @Test
    public void decayScoresTest_whenDecayDayEvents() {
        BucketTransactionEventsData bucketTransactionEventsData = new BucketTransactionEventsData();
        bucketTransactionEventsData.setUserType(UserType.WALLET);
        BucketTransactionsCalculator bucketTransactionsCalculator = new BucketTransactionsCalculator(bucketTransactionEventsData);
        bucketTransactionEventsData.setLastUpdate(DatesCalculation.decreaseTodayDateByDays(3));
        bucketTransactionEventsData.setCurrentDateNumberOfTransactionsContribution(8);
        bucketTransactionEventsData.setCurrentDateTurnOverContribution(5);

        bucketTransactionsCalculator.decayScores(bucketTransactionEventsData);

        Assert.assertTrue((ifTwoNumbersAreEqualOrAlmostEqual(bucketTransactionEventsData.getOldDateNumberOfTransactionsContribution(), 7.464263932294459)
                && (ifTwoNumbersAreEqualOrAlmostEqual(bucketTransactionEventsData.getOldDateTurnOverContribution(), 4.665164957684037)))
                && (bucketTransactionEventsData.getCurrentDateTurnOverContribution() == 0)
                && (bucketTransactionEventsData.getCurrentDateTurnOver() == 0));
    }

    @Test
    public void decayScoresTest_whenDecayMonthEvents() {
        BucketTransactionEventsData bucketTransactionEventsData = new BucketTransactionEventsData();
        bucketTransactionEventsData.setUserType(UserType.WALLET);
        BucketTransactionsCalculator bucketTransactionsCalculator = new BucketTransactionsCalculator(bucketTransactionEventsData);
        bucketTransactionEventsData.setLastUpdate(DatesCalculation.decreaseTodayDateByDays(3));
        bucketTransactionEventsData.setOldMonthBalanceContribution(7);
        Map<Date, BalanceCountAndContribution> currentMonthBalance = new ConcurrentHashMap<>();
        currentMonthBalance.put(DatesCalculation.setDateOnBeginningOfDay(DatesCalculation.decreaseTodayDateByDays(0)),
                new BalanceCountAndContribution(60.0, 8.5));

        bucketTransactionEventsData.setCurrentMonthDayToBalanceCountAndContribution(currentMonthBalance);

        bucketTransactionsCalculator.decayScores(bucketTransactionEventsData);

        Assert.assertTrue((ifTwoNumbersAreEqualOrAlmostEqual(bucketTransactionEventsData.getOldMonthBalanceContribution(), 6.531230940757652))
                && (ifTwoNumbersAreEqualOrAlmostEqual(bucketTransactionEventsData.getCurrentMonthDayToBalanceCountAndContribution()
                .get(DatesCalculation.setDateOnBeginningOfDay(DatesCalculation.decreaseTodayDateByDays(0))).getContribution(), 7.930780428062863))
        );
    }

    @Test
    public void setCurrentDayTransactionsScoresTest() {
        BucketTransactionEventsData bucketTransactionEventsData = new BucketTransactionEventsData();
        bucketTransactionEventsData.setUserType(UserType.WALLET);
        bucketTransactionEventsData.setCurrentDateNumberOfTransactions(8);
        bucketTransactionEventsData.setCurrentDateTurnOver(5);
        BucketTransactionsCalculator bucketTransactionsCalculator = new BucketTransactionsCalculator(bucketTransactionEventsData);
        bucketTransactionsCalculator.setCurrentDayTransactionsScores();

        Assert.assertTrue((ifTwoNumbersAreEqualOrAlmostEqual(bucketTransactionEventsData.getCurrentDateNumberOfTransactionsContribution(), 0.021968710545463798))
                && (ifTwoNumbersAreEqualOrAlmostEqual(bucketTransactionEventsData.getCurrentDateTurnOverContribution(), 0.0013732644979896087)));
    }

    @Test
    // Balance don't affect the fullnode TS
    public void fullNodeTransactionTest() {
        BucketTransactionEventsData bucketTransactionEventsData = new BucketTransactionEventsData();
        bucketTransactionEventsData.setUserType(UserType.FULL_NODE);
        bucketTransactionEventsData.setCurrentDateNumberOfTransactions(8);
        bucketTransactionEventsData.setCurrentDateTurnOver(5);

        BucketTransactionsCalculator bucketTransactionsCalculator = new BucketTransactionsCalculator(bucketTransactionEventsData);
        bucketTransactionsCalculator.setCurrentDayTransactionsScores();

        Assert.assertTrue((ifTwoNumbersAreEqualOrAlmostEqual(bucketTransactionEventsData.getCurrentDateNumberOfTransactionsContribution(), 0.00021972245))
                && (ifTwoNumbersAreEqualOrAlmostEqual(bucketTransactionEventsData.getCurrentDateTurnOverContribution(), 0.00001373265)));
    }
}