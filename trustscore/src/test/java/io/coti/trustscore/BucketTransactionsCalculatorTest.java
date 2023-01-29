package io.coti.trustscore;

import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.trustscore.config.rules.RulesData;
import io.coti.trustscore.config.rules.TransactionEventsScore;
import io.coti.trustscore.data.Buckets.BucketTransactionEventsData;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.BalanceCountAndContribution;
import io.coti.trustscore.services.BucketTransactionService;
import io.coti.trustscore.services.calculationservices.BucketTransactionsCalculator;
import io.coti.trustscore.testutils.BucketUtil;
import io.coti.trustscore.utils.DatesCalculation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.coti.trustscore.testutils.GeneralUtilsFunctions.isTrustScoreValueValid;

@ContextConfiguration(classes = {
        BucketTransactionsCalculator.class,
        BaseNodeRocksDBConnector.class,
        BucketTransactionService.class,
        BucketTransactionEventsData.class
})
@TestPropertySource(locations = "classpath:test.properties")
@ExtendWith(SpringExtension.class)
@SpringBootTest

public class BucketTransactionsCalculatorTest {

    @BeforeAll
    public static void setUp() {
        RulesData rulesData = BucketUtil.generateRulesDataObject();
        BucketTransactionsCalculator.init(rulesData);
    }

    @Test
    void setBucketTransactionEventsDataTest() {
        BucketTransactionEventsData bucketTransactionEventsData = new BucketTransactionEventsData();
        bucketTransactionEventsData.setUserType(UserType.CONSUMER);
        BucketTransactionsCalculator bucketTransactionsCalculator = new BucketTransactionsCalculator(bucketTransactionEventsData);

        TransactionEventsScore transactionEventsScore = bucketTransactionsCalculator.getTransactionEventsScore();
        Assertions.assertEquals(3, transactionEventsScore.getTransactionEventScoreList().size());
    }

    @Test
    void decayScoresTestWhenDecayDayEvents() {
        BucketTransactionEventsData bucketTransactionEventsData = new BucketTransactionEventsData();
        bucketTransactionEventsData.setUserType(UserType.CONSUMER);
        BucketTransactionsCalculator bucketTransactionsCalculator = new BucketTransactionsCalculator(bucketTransactionEventsData);
        bucketTransactionEventsData.setLastUpdate(DatesCalculation.decreaseTodayDateByDays(3));
        bucketTransactionEventsData.setCurrentDateNumberOfTransactionsContribution(8);
        bucketTransactionEventsData.setCurrentDateTurnOverContribution(5);

        bucketTransactionsCalculator.decayScores(bucketTransactionEventsData);

        Assertions.assertTrue((isTrustScoreValueValid(bucketTransactionEventsData.getOldDateNumberOfTransactionsContribution())
                && (isTrustScoreValueValid(bucketTransactionEventsData.getOldDateTurnOverContribution()))
                && (bucketTransactionEventsData.getCurrentDateTurnOverContribution() == 0)
                && (bucketTransactionEventsData.getCurrentDateTurnOver() == 0)));
    }

    @Test
    void decayScoresTestWhenDecayMonthEvents() {
        BucketTransactionEventsData bucketTransactionEventsData = new BucketTransactionEventsData();
        bucketTransactionEventsData.setUserType(UserType.CONSUMER);
        BucketTransactionsCalculator bucketTransactionsCalculator = new BucketTransactionsCalculator(bucketTransactionEventsData);
        bucketTransactionEventsData.setLastUpdate(DatesCalculation.decreaseTodayDateByDays(3));
        bucketTransactionEventsData.setOldMonthBalanceContribution(7);
        Map<Date, BalanceCountAndContribution> currentMonthBalance = new ConcurrentHashMap<>();
        currentMonthBalance.put(DatesCalculation.setDateOnBeginningOfDay(DatesCalculation.decreaseTodayDateByDays(0)),
                new BalanceCountAndContribution(60.0, 8.5));

        bucketTransactionEventsData.setCurrentMonthDayToBalanceCountAndContribution(currentMonthBalance);

        bucketTransactionsCalculator.decayScores(bucketTransactionEventsData);

        Assertions.assertTrue((isTrustScoreValueValid(bucketTransactionEventsData.getOldMonthBalanceContribution()))
                && (isTrustScoreValueValid(bucketTransactionEventsData.getCurrentMonthDayToBalanceCountAndContribution()
                .get(DatesCalculation.setDateOnBeginningOfDay(DatesCalculation.decreaseTodayDateByDays(0))).getContribution()))
        );
    }

    @Test
    void setCurrentDayTransactionsScoresTest() {
        BucketTransactionEventsData bucketTransactionEventsData = new BucketTransactionEventsData();
        bucketTransactionEventsData.setUserType(UserType.CONSUMER);
        bucketTransactionEventsData.setCurrentDateNumberOfTransactions(8);
        bucketTransactionEventsData.setCurrentDateTurnOver(5);
        BucketTransactionsCalculator bucketTransactionsCalculator = new BucketTransactionsCalculator(bucketTransactionEventsData);
        bucketTransactionsCalculator.setCurrentDayTransactionsScores();

        Assertions.assertTrue((isTrustScoreValueValid(bucketTransactionEventsData.getCurrentDateNumberOfTransactionsContribution()))
                && (isTrustScoreValueValid(bucketTransactionEventsData.getCurrentDateTurnOverContribution())));
    }

    @Test
        // Balance don't affect the fullnode TS
    void fullNodeTransactionTest() {
        BucketTransactionEventsData bucketTransactionEventsData = new BucketTransactionEventsData();
        bucketTransactionEventsData.setUserType(UserType.FULL_NODE);
        bucketTransactionEventsData.setCurrentDateNumberOfTransactions(8);
        bucketTransactionEventsData.setCurrentDateTurnOver(5);

        BucketTransactionsCalculator bucketTransactionsCalculator = new BucketTransactionsCalculator(bucketTransactionEventsData);
        bucketTransactionsCalculator.setCurrentDayTransactionsScores();

        Assertions.assertTrue((isTrustScoreValueValid(bucketTransactionEventsData.getCurrentDateNumberOfTransactionsContribution()))
                && (isTrustScoreValueValid(bucketTransactionEventsData.getCurrentDateTurnOverContribution())));
    }
}