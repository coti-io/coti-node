package io.coti.trustscore.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.database.RocksDBConnector;
import io.coti.trustscore.testUtils.BucketUtil;
import io.coti.trustscore.data.Buckets.BucketChargeBackEventsData;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Enums.HighFrequencyEventScoreType;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.ChargeBackEventsData;
import io.coti.trustscore.http.InsertEventRequest;
import lombok.extern.slf4j.Slf4j;
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

import static io.coti.trustscore.testUtils.BucketUtil.generateRulesDataObject;
import static io.coti.trustscore.testUtils.GeneralUtilsFunctions.generateRandomHash;
import static io.coti.trustscore.testUtils.GeneralUtilsFunctions.generateRandomTrustScore;
import static io.coti.trustscore.utils.DatesCalculation.decreaseTodayDateByDays;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {BucketChargeBackEventsService.class,
        RocksDBConnector.class
})

@Slf4j
public class BucketChargeBackEventsServiceTest {
    private Hash user1Hash;
    private Hash user2Hash;
    private Hash user3Hash;

    @Autowired
    private BucketChargeBackEventsService bucketChargeBackEventsService;

    private BucketChargeBackEventsData bucketChargeBackEventsData;

    @Before
    public void setUp() {
        bucketChargeBackEventsService.init(generateRulesDataObject());
        bucketChargeBackEventsData = new BucketChargeBackEventsData();
        bucketChargeBackEventsData.setUserType(UserType.MERCHANT);

        user1Hash =  generateRandomHash(64);
        user2Hash =  generateRandomHash(64);
        user3Hash =  generateRandomHash(64);
    }

    @Test
    public void BehaviorHighFrequencyEventsService_simpleScenarioTest() {
        addPaymentTransactionsAndChargeBacks();
        double bucketSumScore = bucketChargeBackEventsService.getBucketSumScore(bucketChargeBackEventsData);
        Assert.assertTrue(bucketSumScore < 0);
    }

    @Test
    public void BehaviorHighFrequencyEventsService_complicatedScenarioWithDecayTest() {
        addPaymentTransactionsAndChargeBacks();
        Date yesterday = decreaseTodayDateByDays(1);
        bucketChargeBackEventsData.setLastUpdate(yesterday);

        TransactionData transactionData3 =
                BucketUtil.createTransactionWithSpecificHash(generateRandomHash(64), user1Hash, generateRandomTrustScore(), TransactionType.Payment);
        transactionData3.setAmount(new BigDecimal(7));
        bucketChargeBackEventsService.addPaymentTransactionToCalculations(transactionData3, bucketChargeBackEventsData);
        bucketChargeBackEventsData.setLastUpdate(yesterday);
 ;

        TransactionData transactionData4 =
                BucketUtil.createTransactionWithSpecificHash(generateRandomHash(64), user1Hash,  generateRandomTrustScore(), TransactionType.Payment);
        transactionData4.setAmount(new BigDecimal(13.5));

        bucketChargeBackEventsService.addPaymentTransactionToCalculations(transactionData4, bucketChargeBackEventsData);
        TransactionData transactionData5 = BucketUtil.createTransactionWithSpecificHash(generateRandomHash(64), user2Hash,  generateRandomTrustScore(), TransactionType.Payment);
        transactionData5.setAmount(new BigDecimal(7.2));
        bucketChargeBackEventsService.addEventToCalculations(new ChargeBackEventsData(buildChargeBackDataRequest(transactionData5)), bucketChargeBackEventsData);
        TransactionData transactionData6 = BucketUtil.createTransactionWithSpecificHash(generateRandomHash(64), user3Hash,  generateRandomTrustScore(), TransactionType.Payment);
        transactionData6.setAmount(new BigDecimal(5));
        bucketChargeBackEventsService.addEventToCalculations(new ChargeBackEventsData(buildChargeBackDataRequest(transactionData6)), bucketChargeBackEventsData);
        double bucketSumScore = bucketChargeBackEventsService.getBucketSumScore(bucketChargeBackEventsData);
        Assert.assertTrue(bucketSumScore < 0);
    }

    private void addPaymentTransactionsAndChargeBacks() {
        TransactionData transactionData =
                BucketUtil.createTransactionWithSpecificHash(generateRandomHash(64), user1Hash, generateRandomTrustScore(), TransactionType.Payment);
        transactionData.setAmount(new BigDecimal(3));
        bucketChargeBackEventsService.addPaymentTransactionToCalculations(transactionData, bucketChargeBackEventsData);

        TransactionData transactionData1 =
                BucketUtil.createTransactionWithSpecificHash(generateRandomHash(64), user1Hash, generateRandomTrustScore(), TransactionType.Payment);
        transactionData1.setAmount(new BigDecimal(4));
        bucketChargeBackEventsService.addPaymentTransactionToCalculations(transactionData1, bucketChargeBackEventsData);

        TransactionData transactionData2 =
                BucketUtil.createTransactionWithSpecificHash(generateRandomHash(64), user1Hash,  generateRandomTrustScore(), TransactionType.Payment);
        transactionData2.setAmount(new BigDecimal(5));
        bucketChargeBackEventsService.addPaymentTransactionToCalculations(transactionData2, bucketChargeBackEventsData);

        bucketChargeBackEventsService.addEventToCalculations(new ChargeBackEventsData(buildChargeBackDataRequest(transactionData1)), bucketChargeBackEventsData);
        bucketChargeBackEventsService.addEventToCalculations(new ChargeBackEventsData(buildChargeBackDataRequest(transactionData2)), bucketChargeBackEventsData);
    }

    private InsertEventRequest buildChargeBackDataRequest(TransactionData transactionData) {
        InsertEventRequest insertEventRequest = new InsertEventRequest();
        insertEventRequest.setUserHash(transactionData.getSenderHash());
        insertEventRequest.eventType = EventType.HIGH_FREQUENCY_EVENTS;
        insertEventRequest.setHighFrequencyEventScoreType(HighFrequencyEventScoreType.CHARGE_BACK);
        insertEventRequest.setTransactionData(transactionData);
        insertEventRequest.uniqueIdentifier = generateRandomHash(72);
        return insertEventRequest;
    }
}