package io.coti.trustscore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.database.RocksDBConnector;
import io.coti.trustscore.data.Buckets.BucketTransactionEventsData;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.BalanceCountAndContribution;
import io.coti.trustscore.data.Events.TransactionEventData;
import io.coti.trustscore.model.BucketTransactionEvents;
import io.coti.trustscore.services.BucketTransactionService;
import io.coti.trustscore.services.calculationServices.BucketTransactionsCalculator;
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
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.coti.trustscore.BucketUtil.generateRulesDataObject;
import static io.coti.trustscore.utils.BucketBuilder.buildTransactionDataRequest;
import static io.coti.trustscore.utils.DatesCalculation.*;
import static io.coti.trustscore.utils.MathCalculation.ifTwoNumbersAreEqualOrAlmostEqual;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {BucketTransactionService.class,
        BucketTransactionsCalculator.class,
        RocksDBConnector.class,
        BucketTransactionService.class,
        BucketTransactionEvents.class
})
public class BucketTransactionServiceTest {

    @Autowired
    private BucketTransactionService bucketTransactionService;

    private BucketTransactionEventsData bucketTransactionEventsDataForWallet;
    private BucketTransactionEventsData bucketTransactionEventsDataForNode;

    @Before
    public void setUp() {
        BucketTransactionService.init(generateRulesDataObject());

        initialBucketTransactionEventsDataForWallet();
        initialBucketTransactionEventsDataForNode();

    }

    private void initialBucketTransactionEventsDataForNode() {
        bucketTransactionEventsDataForNode = new BucketTransactionEventsData();
        bucketTransactionEventsDataForNode.setUserType(UserType.FULL_NODE);
        TransactionData transactionData = BucketUtil.createTransactionWithSpecificHash(new Hash("1234"), new Hash("dddd"), 70.45, TransactionType.Payment);
        transactionData.setAmount(new BigDecimal(-8));

        TransactionData transactionData2 = BucketUtil.createTransactionWithSpecificHash(new Hash("2345"), new Hash("dddd"), 70.45, TransactionType.Payment);
        transactionData2.setAmount(new BigDecimal(-5));

        bucketTransactionService.addEventToCalculations(new TransactionEventData(buildTransactionDataRequest(new Hash("8765"),
                null,
                transactionData)), bucketTransactionEventsDataForNode);
        bucketTransactionService.addEventToCalculations(new TransactionEventData(buildTransactionDataRequest(new Hash("8765"),
                null,
                transactionData2)), bucketTransactionEventsDataForNode);
    }

    private void initialBucketTransactionEventsDataForWallet() {
        bucketTransactionEventsDataForWallet = new BucketTransactionEventsData();
        bucketTransactionEventsDataForWallet.setUserType(UserType.CONSUMER);

        TransactionData transactionData = BucketUtil.createTransactionWithSpecificHash(new Hash("1234"), new Hash("abcd"), 70.45, TransactionType.Payment);
        transactionData.setAmount(new BigDecimal(8));

        TransactionData transactionData2 = BucketUtil.createTransactionWithSpecificHash(new Hash("2345"), new Hash("abcd"), 70.45, TransactionType.Payment);
        transactionData2.setAmount(new BigDecimal(5));

        TransactionData transactionDatatemp = BucketUtil.createTransactionWithSpecificHash(new Hash("1234"),
                new Hash("2d543b3026626fb4de4b6250ad10ffa7a8c1845927e005608700c3d52834502d8c80ebaae318184cd525352ed07694d6ed8ed2a8a2cf1171200e2108cbe53702"),
                70.45,
                TransactionType.Payment);
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonInString = mapper.writeValueAsString(transactionDatatemp);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        bucketTransactionService.addEventToCalculations(new TransactionEventData(buildTransactionDataRequest(new Hash("8765"),
                null,
                transactionData)), bucketTransactionEventsDataForWallet);
        bucketTransactionService.addEventToCalculations(new TransactionEventData(buildTransactionDataRequest(new Hash("8765"),
                null,
                transactionData2)), bucketTransactionEventsDataForWallet);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void addEventToCalculationsTest() {
        Assert.assertTrue(
                (ifTwoNumbersAreEqualOrAlmostEqual(bucketTransactionEventsDataForWallet.getCurrentMonthDayToBalanceCountAndContribution()
                        .get(setDateOnBeginningOfDay(decreaseTodayDateByDays(0))).getContribution(), 0.03568973444951873))
                        && (ifTwoNumbersAreEqualOrAlmostEqual(bucketTransactionEventsDataForWallet.getCurrentDateNumberOfTransactionsContribution(), 0.0))
                        && (ifTwoNumbersAreEqualOrAlmostEqual(bucketTransactionEventsDataForWallet.getCurrentDateTurnOverContribution(), 0.0))
        );
    }

    @Test
    public void getBucketSumScoreTest() {
        double sumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForWallet);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore, 0.02676730083));
    }

    @Test
    public void BucketTransactionService_simulationOfZeroDayDecayedTest() {
        decayDailyEventsDataForWallet(0);
        double sumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForWallet);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore, 0.02676730083));
    }

    @Test
    public void BucketTransactionService_simulationOfDayDecayedTest() {
        decayDailyEventsDataForWallet(1);
        double sumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForWallet);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore, 0.05292323566));
    }

    @Test
    public void BucketTransactionService_simulationOfTwoDayDecayedTest() {
        decayDailyEventsDataForWallet(2);
        double sumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForWallet);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore, 0.07848176812));
    }

    @Test
    public void BucketTransactionService_simulationOfThreeDayDecayed() {
        decayDailyEventsDataForWallet(3);

        double sumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForWallet);

        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore, 0.1034565429));
    }

    @Test
    public void BucketTransactionService_simulationOfThreeDayDecayedAndAddingNewTransactionTest() {
        decayDailyEventsDataForWallet(3);

        TransactionData transactionData = BucketUtil.createTransactionWithSpecificHash(new Hash("1236"), new Hash("abcd"), 70.45, TransactionType.Payment);
        transactionData.setAmount(new BigDecimal(7));
        bucketTransactionService.addEventToCalculations(new TransactionEventData(buildTransactionDataRequest(new Hash("8765"),
                null,
                transactionData)), bucketTransactionEventsDataForWallet);

        double sumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForWallet);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore, 0.1178458163));
    }

    @Test
    public void BucketTransactionService_complicatedScenarioTest() {
        // Decay 3 days, and adding transaction of 7 coti
        decayDailyEventsDataForWallet(3);
        TransactionData transactionData = BucketUtil.createTransactionWithSpecificHash(new Hash("1236"), new Hash("abcd"), 70.45, TransactionType.Payment);
        transactionData.setAmount(new BigDecimal(7));
        bucketTransactionService.addEventToCalculations(new TransactionEventData(buildTransactionDataRequest(new Hash("8765"),
                null,
                transactionData)), bucketTransactionEventsDataForWallet);
        double sumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForWallet);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore, 0.1178458163));

        // Decay 2 days
        decayDailyEventsDataForWallet(2);
        sumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForWallet);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore, 0.1938972193));

        // Decay 2 days
        decayDailyEventsDataForWallet(26);
        sumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForWallet);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore, 0.9200733628));

        // Adding transaction of 11 coti
        TransactionData transactionData2 = BucketUtil.createTransactionWithSpecificHash(new Hash("3456"), new Hash("abcd"), 70.45, TransactionType.Payment);
        transactionData2.setAmount(new BigDecimal(11));
        bucketTransactionService.addEventToCalculations(new TransactionEventData(buildTransactionDataRequest(new Hash("8765"),
                null,
                transactionData2)), bucketTransactionEventsDataForWallet);
        sumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForWallet);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore, 0.9426197695));

        // Adding transaction of -3 coti
        TransactionData transactionData3 = BucketUtil.createTransactionWithSpecificHash(new Hash("3457"), new Hash("abcd"), 70.45, TransactionType.Payment);
        transactionData3.setAmount(new BigDecimal(-3));
        bucketTransactionService.addEventToCalculations(new TransactionEventData(buildTransactionDataRequest(new Hash("8765"),
                null,
                transactionData3)), bucketTransactionEventsDataForWallet);
        sumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForWallet);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore, 0.9391583623));

    }

    @Test
    public void BucketTransactionEventsDataForNodeTest() {

        double sumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForNode);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore, 0.00011810082));

        decayDailyEventsDataForNode(3);
        sumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForNode);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore, 0.00011019196));

        TransactionData transactionData = BucketUtil.createTransactionWithSpecificHash(new Hash("8765"), new Hash("dddd"), 70.45, TransactionType.Payment);
        transactionData.setAmount(new BigDecimal(-12));
        bucketTransactionService.addEventToCalculations(new TransactionEventData(buildTransactionDataRequest(new Hash("8765"),
                null,
                transactionData)),
                bucketTransactionEventsDataForNode);
        sumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForNode);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore, 0.00018434828));

        decayDailyEventsDataForNode(29);
        sumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForNode);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(sumScore, 0.0000943286));
    }

    public void decayDailyEventsDataForWallet(int numOfDays) {
        // simulation of moving to a new day
        bucketTransactionEventsDataForWallet.setLastUpdate(decreaseTodayDateByDays(numOfDays));
        ConcurrentHashMap<Date, BalanceCountAndContribution> CurrentMonthBalanceWithDateChangedEntry = new ConcurrentHashMap<>();
        for (Map.Entry<Date, BalanceCountAndContribution> currentMonthBalanceEntry
                : bucketTransactionEventsDataForWallet.getCurrentMonthDayToBalanceCountAndContribution().entrySet()) {
            Date dateBeforeDecayed = currentMonthBalanceEntry.getKey();
            Date oldDate = addToDateByDays(dateBeforeDecayed.getTime(), -numOfDays);
            BalanceCountAndContribution value = currentMonthBalanceEntry.getValue();
            CurrentMonthBalanceWithDateChangedEntry.put(oldDate, value);
        }
        bucketTransactionEventsDataForWallet.setCurrentMonthDayToBalanceCountAndContribution(CurrentMonthBalanceWithDateChangedEntry);
    }

    public void decayDailyEventsDataForNode(int numOfDays) {
        // simulation of moving to a new day
        bucketTransactionEventsDataForNode.setLastUpdate(decreaseTodayDateByDays(numOfDays));
    }


}