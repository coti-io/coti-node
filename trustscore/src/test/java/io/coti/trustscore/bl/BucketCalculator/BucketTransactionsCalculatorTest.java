package io.coti.trustscore.bl.BucketCalculator;

import io.coti.basenode.database.RocksDBConnector;
import io.coti.trustscore.data.Buckets.BucketTransactionEventsData;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.rulesData.RulesData;
import io.coti.trustscore.rulesData.TransactionEventsScore;
import io.coti.trustscore.services.BucketTransactionService;
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

import java.util.HashMap;
import java.util.Map;

import static io.coti.trustscore.util.bucketUtil.generateRulesDataObject;
import static io.coti.trustscore.utils.DatesCalculation.decreaseTodayDateByDays;
import static io.coti.trustscore.utils.DatesCalculation.setDateOnBeginningOfDay;
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

        RulesData rulesData = generateRulesDataObject();
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
        bucketTransactionEventsData.setLastUpdate(decreaseTodayDateByDays(3));
        bucketTransactionEventsData.setCurrentDateNumberOfTransactionsContribution(8);
        bucketTransactionEventsData.setCurrentDateTurnOverContribution(5);

        bucketTransactionsCalculator.decayScores();

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
        bucketTransactionEventsData.setLastUpdate(decreaseTodayDateByDays(3));
        bucketTransactionEventsData.setOldMonthBalanceContribution(7);
        Map<Long, Pair<Double, Double>> currentMonthBalance = new HashMap<>();
        currentMonthBalance.put(setDateOnBeginningOfDay(decreaseTodayDateByDays(0)).getTime(), new Pair<>(60.0, 8.5));

        bucketTransactionEventsData.setCurrentMonthBalance(currentMonthBalance);

        bucketTransactionsCalculator.decayScores();

        Assert.assertTrue((ifTwoNumbersAreEqualOrAlmostEqual(bucketTransactionEventsData.getOldMonthBalanceContribution(), 6.531230940757652))
                && (ifTwoNumbersAreEqualOrAlmostEqual(bucketTransactionEventsData.getCurrentMonthBalance()
                .get(setDateOnBeginningOfDay(decreaseTodayDateByDays(0)).getTime()).getValue(), 7.930780428062863))
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
}