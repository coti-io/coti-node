package io.coti.trustscore;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.trustscore.data.Buckets.BucketTransactionEventsData;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.BalanceCountAndContribution;
import io.coti.trustscore.data.Events.TransactionEventData;
import io.coti.trustscore.model.BucketTransactionEvents;
import io.coti.trustscore.services.BucketTransactionService;
import io.coti.trustscore.services.calculationservices.BucketTransactionsCalculator;
import io.coti.trustscore.testutils.BucketUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.coti.trustscore.testutils.BucketUtil.generateRulesDataObject;
import static io.coti.trustscore.testutils.GeneralUtilsFunctions.*;
import static io.coti.trustscore.utils.BucketBuilder.buildTransactionDataRequest;
import static io.coti.trustscore.utils.DatesCalculation.*;
import static io.coti.trustscore.utils.MathCalculation.ifTwoNumbersAreEqualOrAlmostEqual;

@TestPropertySource(locations = "classpath:test.properties")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {BucketTransactionService.class,
        BucketTransactionsCalculator.class,
        BaseNodeRocksDBConnector.class,
        BucketTransactionEvents.class,
        BucketTransactionEventsData.class
})
public class BucketTransactionServiceTest {
    private static Hash walletHash;
    private static Hash nodeHash;

    protected static BucketTransactionService bucketTransactionService;
    private static BucketTransactionEventsData bucketTransactionEventsDataForWallet;
    private static BucketTransactionEventsData bucketTransactionEventsDataForNode;

    @BeforeAll
    public static void setUp() {
        walletHash = generateRandomHash(64);
        nodeHash = generateRandomHash(64);
        bucketTransactionService = new BucketTransactionService();
        bucketTransactionService.init(generateRulesDataObject());
        initialBucketTransactionEventsDataForWallet();
        initialBucketTransactionEventsDataForNode();
    }

    private static void initialBucketTransactionEventsDataForNode() {
        bucketTransactionEventsDataForNode = new BucketTransactionEventsData();
        bucketTransactionEventsDataForNode.setUserType(UserType.FULL_NODE);
        TransactionData transactionData = BucketUtil.createTransactionWithSpecificHash(new Hash("1234"), nodeHash, 70.45, TransactionType.Payment);
        transactionData.setAmount(new BigDecimal(-8));

        TransactionData transactionData2 = BucketUtil.createTransactionWithSpecificHash(new Hash("2345"), nodeHash, 70.45, TransactionType.Payment);
        transactionData2.setAmount(new BigDecimal(-5));

        bucketTransactionService.addEventToCalculations(new TransactionEventData(buildTransactionDataRequest(new Hash("8765"),
                null,
                transactionData)), bucketTransactionEventsDataForNode);
        bucketTransactionService.addEventToCalculations(new TransactionEventData(buildTransactionDataRequest(new Hash("8765"),
                null,
                transactionData2)), bucketTransactionEventsDataForNode);
    }

    private static void initialBucketTransactionEventsDataForWallet() {
        bucketTransactionEventsDataForWallet = new BucketTransactionEventsData();
        bucketTransactionEventsDataForWallet.setUserType(UserType.CONSUMER);

        TransactionData transactionData = BucketUtil.createTransactionWithSpecificHash(new Hash("1234"), walletHash, 70.45, TransactionType.Payment);
        transactionData.setAmount(new BigDecimal(8));

        TransactionData transactionData2 = BucketUtil.createTransactionWithSpecificHash(new Hash("2345"), walletHash, 70.45, TransactionType.Payment);
        transactionData2.setAmount(new BigDecimal(5));

//        TransactionData transactionDatatemp = BucketUtil.createTransactionWithSpecificHash(new Hash("1234"),
//                new Hash("2d543b3026626fb4de4b6250ad10ffa7a8c1845927e005608700c3d52834502d8c80ebaae318184cd525352ed07694d6ed8ed2a8a2cf1171200e2108cbe53702"),
//                70.45,
//                TransactionType.Payment);
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            String jsonInString = mapper.writeValueAsString(transactionDatatemp);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }

        bucketTransactionService.addEventToCalculations(new TransactionEventData(buildTransactionDataRequest(new Hash("8765"),
                null,
                transactionData)), bucketTransactionEventsDataForWallet);
        bucketTransactionService.addEventToCalculations(new TransactionEventData(buildTransactionDataRequest(new Hash("8765"),
                null,
                transactionData2)), bucketTransactionEventsDataForWallet);
    }

    @Test
    void addEventToCalculationsTest() {
        Assertions.assertTrue(
                (ifTwoNumbersAreEqualOrAlmostEqual(bucketTransactionEventsDataForWallet.getCurrentMonthDayToBalanceCountAndContribution()
                        .get(setDateOnBeginningOfDay(decreaseTodayDateByDays(0))).getContribution(), 0.03568973444951873))
                        && (ifTwoNumbersAreEqualOrAlmostEqual(bucketTransactionEventsDataForWallet.getCurrentDateNumberOfTransactionsContribution(), 0.0))
                        && (ifTwoNumbersAreEqualOrAlmostEqual(bucketTransactionEventsDataForWallet.getCurrentDateTurnOverContribution(), 0.0))
        );
    }

    @Test
    void getBucketSumScoreTest() {
        double bucketSumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForWallet);
        Assertions.assertTrue(isTrustScoreValueValid(bucketSumScore));
    }

    @Test
    void bucketTransactionServiceSimulationOfZeroDayDecayedTest() {
        decayDailyEventsDataForWallet(0);
        double bucketSumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForWallet);
        Assertions.assertTrue(isTrustScoreValueValid(bucketSumScore));
    }

    @Test
    void bucketTransactionServiceSimulationOfDayDecayedTest() {
        decayDailyEventsDataForWallet(1);
        double bucketSumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForWallet);
        Assertions.assertTrue(isTrustScoreValueValid(bucketSumScore));
    }

    @Test
    void bucketTransactionServiceSimulationOfTwoDayDecayedTest() {
        decayDailyEventsDataForWallet(2);
        double bucketSumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForWallet);
        Assertions.assertTrue(isTrustScoreValueValid(bucketSumScore));
    }

    @Test
    void bucketTransactionServiceSimulationOfThreeDayDecayed() {
        decayDailyEventsDataForWallet(3);
        double bucketSumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForWallet);
        Assertions.assertTrue(isTrustScoreValueValid(bucketSumScore));
    }

    @Test
    void bucketTransactionServiceSimulationOfThreeDayDecayedAndAddingNewTransactionTest() {
        decayDailyEventsDataForWallet(3);

        TransactionData transactionData =
                BucketUtil.createTransactionWithSpecificHash(generateRandomHash(64), walletHash, 70.45, TransactionType.Payment);
        transactionData.setAmount(new BigDecimal(7));
        bucketTransactionService.addEventToCalculations(new TransactionEventData(buildTransactionDataRequest(generateRandomHash(64),
                null,
                transactionData)), bucketTransactionEventsDataForWallet);

        double bucketSumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForWallet);
        Assertions.assertTrue(isTrustScoreValueValid(bucketSumScore));
    }

    @Test
    void bucketTransactionServiceComplicatedScenarioTest() {
        // Decay 3 days, and adding transaction of 7 coti
        decayDailyEventsDataForWallet(3);
        TransactionData transactionData =
                BucketUtil.createTransactionWithSpecificHash(generateRandomHash(64), walletHash, generateRandomTrustScore(), TransactionType.Payment);
        transactionData.setAmount(new BigDecimal(7));
        bucketTransactionService.addEventToCalculations(new TransactionEventData(buildTransactionDataRequest(generateRandomHash(64),
                null,
                transactionData)), bucketTransactionEventsDataForWallet);

        // Decay 2 days
        decayDailyEventsDataForWallet(2);
        bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForWallet);


        // Decay 2 days
        decayDailyEventsDataForWallet(26);

        TransactionData transactionData2 =
                BucketUtil.createTransactionWithSpecificHash(generateRandomHash(64), walletHash, generateRandomTrustScore(), TransactionType.Payment);
        transactionData2.setAmount(new BigDecimal(11));
        bucketTransactionService.addEventToCalculations(new TransactionEventData(buildTransactionDataRequest(generateRandomHash(64),
                null,
                transactionData2)), bucketTransactionEventsDataForWallet);

        // Adding transaction of -3 coti
        TransactionData transactionData3 =
                BucketUtil.createTransactionWithSpecificHash(generateRandomHash(64), walletHash, generateRandomTrustScore(), TransactionType.Payment);
        transactionData3.setAmount(new BigDecimal(-3));
        bucketTransactionService.addEventToCalculations(new TransactionEventData(buildTransactionDataRequest(generateRandomHash(64),
                null,
                transactionData3)), bucketTransactionEventsDataForWallet);
        double bucketSumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForWallet);
        Assertions.assertTrue(isTrustScoreValueValid(bucketSumScore));
    }

    @Test
    void bucketTransactionEventsDataForNodeTest() {
        decayDailyEventsDataForNode(3);

        TransactionData transactionData =
                BucketUtil.createTransactionWithSpecificHash(generateRandomHash(64), nodeHash, generateRandomTrustScore(), TransactionType.Payment);
        transactionData.setAmount(new BigDecimal(-12));
        bucketTransactionService.addEventToCalculations(new TransactionEventData(buildTransactionDataRequest(generateRandomHash(64),
                        null,
                        transactionData)),
                bucketTransactionEventsDataForNode);

        decayDailyEventsDataForNode(29);
        double bucketSumScore = bucketTransactionService.getBucketSumScore(bucketTransactionEventsDataForNode);
        Assertions.assertTrue(isTrustScoreValueValid(bucketSumScore));
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