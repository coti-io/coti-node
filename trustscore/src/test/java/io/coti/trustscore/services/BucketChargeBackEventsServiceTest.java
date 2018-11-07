package io.coti.trustscore.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.database.RocksDBConnector;
import io.coti.trustscore.data.Buckets.BucketChargeBackEventsData;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.ChargeBackEventsData;
import io.coti.trustscore.util.BucketUtil;
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

import static io.coti.trustscore.util.BucketUtil.generateRulesDataObject;
import static io.coti.trustscore.utils.DatesCalculation.decreaseTodayDateByDays;
import static io.coti.trustscore.utils.MathCalculation.ifTwoNumbersAreEqualOrAlmostEqual;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {BucketChargeBackEventsService.class,
        RocksDBConnector.class
})
public class BucketChargeBackEventsServiceTest {

    @Autowired
    private BucketChargeBackEventsService bucketChargeBackEventsService;

    private BucketChargeBackEventsData bucketChargeBackEventsData;

    @Before
    public void setUp() {
        bucketChargeBackEventsService.init(generateRulesDataObject());
        bucketChargeBackEventsData = new BucketChargeBackEventsData();
        bucketChargeBackEventsData.setUserType(UserType.MERCHANT);
    }

    @Test
    public void BehaviorHighFrequencyEventsService_simpleScenarioTest() {
        addPaymentTransactionsAndChargeBacks();

        // Score: ((0.0+9.0)/(0.0+12.0) - 0.02) *(-5) + ((0.0+2)/(0.0+3) - 0.02 ) *(-5)
        Assert.assertTrue(
                (ifTwoNumbersAreEqualOrAlmostEqual(bucketChargeBackEventsService.getBucketSumScore(bucketChargeBackEventsData), -3.65 - 3.23333333)));
    }

    @Test
    public void BehaviorHighFrequencyEventsService_complicatedScenarioWithDecayTest() {
        addPaymentTransactionsAndChargeBacks();
        Date yesterday = decreaseTodayDateByDays(1);

        bucketChargeBackEventsData.setLastUpdate(yesterday);
        TransactionData transactionData3 = BucketUtil.createTransactionWithSpecificHash(new Hash("3333"), new Hash("abcd"), 70.45);
        transactionData3.setAmount(new BigDecimal(7));
        bucketChargeBackEventsService.addPaymentTransactionToCalculations(transactionData3, bucketChargeBackEventsData);

        // ChargeBack amount Score: ((exp(-ln(2)/30) * 9) )/(((exp(-ln(2)/30)* 12)+7.0)) - 0.02) * (-5) = ((8.794439715908213 )/(18.72591962121095) - 0.02) * (-5) =  -2.24819968625
        // ChargeBack number Score: ((exp(-ln(2)/30) * 2) )/(((exp(-ln(2)/30)* 3)+1)) - 0.02) * (-5) = ((1.95431993687 )/(3.9314799053) - 0.02) * (-5) = -2.38547618701
        Assert.assertTrue(
                (ifTwoNumbersAreEqualOrAlmostEqual(bucketChargeBackEventsService.getBucketSumScore(bucketChargeBackEventsData),
                        -2.24819968625 - 2.38547618701)));


        bucketChargeBackEventsData.setLastUpdate(yesterday);
        double bucketSumScore = bucketChargeBackEventsService.getBucketSumScore(bucketChargeBackEventsData);

        // ChargeBack amount Score: ((8.794439715908213 * exp(-ln(2)/30) )/(18.72591962121095 * exp(-ln(2)/30)) - 0.02) * (-5) = ((8.59357443519 )/(18.298219026) - 0.02) * (-5) =  -2.24819968625
        // ChargeBack number Score: ((1.95431993687 * exp(-ln(2)/30) )/(3.9314799053 * exp(-ln(2)/30) ) - 0.02) * (-5) = ((1.90968320782)/(3.84168478016) - 0.02) * (-5) = -2.38547618701
        Assert.assertTrue(
                (ifTwoNumbersAreEqualOrAlmostEqual(bucketSumScore, -2.248199686264461 - 2.38547618701)));

        TransactionData transactionData4 = BucketUtil.createTransactionWithSpecificHash(new Hash("4444"), new Hash("abcd"), 70.45);
        transactionData4.setAmount(new BigDecimal(13.5));

        bucketChargeBackEventsService.addPaymentTransactionToCalculations(transactionData4, bucketChargeBackEventsData);
        bucketSumScore = bucketChargeBackEventsService.getBucketSumScore(bucketChargeBackEventsData);

        //((8.59357443519 )/(18.298219026 + 13.5) - 0.02) * (-5) = ((8.59357443519 )/(31.798219026) - 0.02) * (-5) =   -1.25126662725
        //((1.90968320782)/(3.84168478016 +1) - 0.02) * (-5) = ((1.90968320782)/(4.84168478016) - 0.02) * (-5)  = -1.53453120092
        Assert.assertTrue(
                (ifTwoNumbersAreEqualOrAlmostEqual(bucketSumScore, -1.25126662725 - 1.87212674361)));


        TransactionData transactionData5 = BucketUtil.createTransactionWithSpecificHash(new Hash("5555"), new Hash("aeed"), 70.45);
        transactionData5.setAmount(new BigDecimal(7.2));

        bucketChargeBackEventsService.addEventToCalculations(new ChargeBackEventsData(transactionData5, null), bucketChargeBackEventsData);
        TransactionData transactionData6 = BucketUtil.createTransactionWithSpecificHash(new Hash("6666"), new Hash("accd"), 70.45);
        transactionData6.setAmount(new BigDecimal(5));

        bucketChargeBackEventsService.addEventToCalculations(new ChargeBackEventsData(transactionData6, null), bucketChargeBackEventsData);
        bucketSumScore = bucketChargeBackEventsService.getBucketSumScore(bucketChargeBackEventsData);

        //((8.59357443519 +12.2)/(31.798219026) - 0.02) * (-5) =  ((20.7935744352)/(31.798219026) - 0.02) * (-5) = -3.16961305886
        //((1.90968320782 + 2)/(4.84168478016) - 0.02) * (-5)  = ((3.90968320782 )/(4.84168478016) - 0.02) * (-5)  = -3.93752349166
        Assert.assertTrue(
                (ifTwoNumbersAreEqualOrAlmostEqual(bucketSumScore, -3.16961305886 - 3.93752349166)));
    }

    public void addPaymentTransactionsAndChargeBacks() {
        TransactionData transactionData = BucketUtil.createTransactionWithSpecificHash(new Hash("0000"), new Hash("abcd"), 70.45);
        transactionData.setAmount(new BigDecimal(3));
        bucketChargeBackEventsService.addPaymentTransactionToCalculations(transactionData, bucketChargeBackEventsData);

        TransactionData transactionData1 = BucketUtil.createTransactionWithSpecificHash(new Hash("1111"), new Hash("abcd"), 70.45);
        transactionData1.setAmount(new BigDecimal(4));
        bucketChargeBackEventsService.addPaymentTransactionToCalculations(transactionData1, bucketChargeBackEventsData);

        TransactionData transactionData2 = BucketUtil.createTransactionWithSpecificHash(new Hash("2222"), new Hash("abcd"), 70.45);
        transactionData2.setAmount(new BigDecimal(5));
        bucketChargeBackEventsService.addPaymentTransactionToCalculations(transactionData2, bucketChargeBackEventsData);

        bucketChargeBackEventsService.addEventToCalculations(new ChargeBackEventsData(transactionData1, null), bucketChargeBackEventsData);
        bucketChargeBackEventsService.addEventToCalculations(new ChargeBackEventsData(transactionData2, null), bucketChargeBackEventsData);
    }

}