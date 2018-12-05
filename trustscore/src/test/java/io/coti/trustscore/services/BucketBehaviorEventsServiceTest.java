package io.coti.trustscore.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.database.RocksDBConnector;
import io.coti.trustscore.data.Buckets.BucketBehaviorEventsData;
import io.coti.trustscore.data.Enums.BehaviorEventsScoreType;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.BehaviorEventsData;
import io.coti.trustscore.http.InsertEventRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static io.coti.trustscore.BucketUtil.generateRulesDataObject;
import static io.coti.trustscore.utils.DatesCalculation.addToDateByDays;
import static io.coti.trustscore.utils.DatesCalculation.decreaseTodayDateByDays;
import static io.coti.trustscore.utils.MathCalculation.ifTwoNumbersAreEqualOrAlmostEqual;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {BucketBehaviorEventsService.class,
        RocksDBConnector.class
})
public class BucketBehaviorEventsServiceTest {

    @Autowired
    private BucketBehaviorEventsService bucketBehaviorEventsService;

    private BucketBehaviorEventsData bucketBehaviorEventsData;

    @Before
    public void setUp() {
        bucketBehaviorEventsService.init(generateRulesDataObject());
        bucketBehaviorEventsData = new BucketBehaviorEventsData();
        bucketBehaviorEventsData.setUserType(UserType.MERCHANT);
    }

    @Test
    public void BucketBehaviorEventsService_complicatedScenarioWithDecayTest() {
        addBehaviorEvents();

        // bucketBehaviorEventsData.setLastUpdate(decreaseTodayDateByDays(3));
        performSimulationOfDecay(3);
        double bucketSumScore = bucketBehaviorEventsService.getBucketSumScore(bucketBehaviorEventsData);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(bucketSumScore, -23.4518392424));

        // bucketBehaviorEventsData.setLastUpdate(decreaseTodayDateByDays(2));
        performSimulationOfDecay(2);
        bucketSumScore = bucketBehaviorEventsService.getBucketSumScore(bucketBehaviorEventsData);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(bucketSumScore, -23.0933720855));

        BehaviorEventsData behaviorEventsData = new BehaviorEventsData(buildBehaviorEventsDataRequest(BehaviorEventsScoreType.INCORRECT_TRANSACTION));
        bucketBehaviorEventsService.addEventToCalculations(behaviorEventsData, bucketBehaviorEventsData);
        bucketSumScore = bucketBehaviorEventsService.getBucketSumScore(bucketBehaviorEventsData);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(bucketSumScore, -25.0933720855));

        // bucketBehaviorEventsData.setLastUpdate(decreaseTodayDateByDays(2));
        performSimulationOfDecay(2);
        bucketSumScore = bucketBehaviorEventsService.getBucketSumScore(bucketBehaviorEventsData);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(bucketSumScore, -24.7098136935));

        BehaviorEventsData behaviorEventsData1 = new BehaviorEventsData(buildBehaviorEventsDataRequest(BehaviorEventsScoreType.INCORRECT_TRANSACTION));

        bucketBehaviorEventsService.addEventToCalculations(behaviorEventsData1, bucketBehaviorEventsData);
        BehaviorEventsData behaviorEventsData2 = new BehaviorEventsData(buildBehaviorEventsDataRequest(BehaviorEventsScoreType.DOUBLE_SPENDING));

        bucketBehaviorEventsService.addEventToCalculations(behaviorEventsData2, bucketBehaviorEventsData);

        for (int i = 0; i < 5; i++) {
            addFillingTheQuestionnaireEvent(UserType.MERCHANT);
        }
        bucketSumScore = bucketBehaviorEventsService.getBucketSumScore(bucketBehaviorEventsData);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(bucketSumScore, -46.7098136935));

        // bucketBehaviorEventsData.setLastUpdate(decreaseTodayDateByDays(2));
        performSimulationOfDecay(2);
        bucketSumScore = bucketBehaviorEventsService.getBucketSumScore(bucketBehaviorEventsData);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(bucketSumScore, -42.448300371));
    }

    @Test
    public void BucketBehaviorEventsService_simpleScenarioTest() {
        addBehaviorEvents();
        double bucketSumScore = bucketBehaviorEventsService.getBucketSumScore(bucketBehaviorEventsData);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(bucketSumScore, -24));

        for (int i = 0; i < 8; i++) {
            addFillingTheQuestionnaireEvent(UserType.MERCHANT);
        }
        bucketSumScore = bucketBehaviorEventsService.getBucketSumScore(bucketBehaviorEventsData);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(bucketSumScore, -44));

        for (int i = 0; i < 6; i++) {
            addFillingTheQuestionnaireEvent(UserType.MERCHANT);
        }
        bucketSumScore = bucketBehaviorEventsService.getBucketSumScore(bucketBehaviorEventsData);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(bucketSumScore, -54));
    }

    @Test
    public void BucketBehaviorEventsService_withLargeDecayTest() {
        addBehaviorEvents();

        double bucketSumScore = bucketBehaviorEventsService.getBucketSumScore(bucketBehaviorEventsData);
        performSimulationOfDecay(400);
        bucketSumScore = bucketBehaviorEventsService.getBucketSumScore(bucketBehaviorEventsData);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(bucketSumScore, -0.0));


        addBehaviorEvents();
        bucketSumScore = bucketBehaviorEventsService.getBucketSumScore(bucketBehaviorEventsData);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(bucketSumScore, -24.0));

        performSimulationOfDecay(1);
        bucketBehaviorEventsService.getBucketSumScore(bucketBehaviorEventsData);
        addBehaviorEvents();
        bucketBehaviorEventsService.getBucketSumScore(bucketBehaviorEventsData);
        bucketSumScore = bucketBehaviorEventsService.getBucketSumScore(bucketBehaviorEventsData);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(bucketSumScore, -47.81587071031064));

        performSimulationOfDecay(20);
        bucketSumScore = bucketBehaviorEventsService.getBucketSumScore(bucketBehaviorEventsData);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(bucketSumScore, -40.98986745129428));

        performSimulationOfDecay(350);
        bucketSumScore = bucketBehaviorEventsService.getBucketSumScore(bucketBehaviorEventsData);
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(bucketSumScore, -1.38881206843));

    }

    public void performSimulationOfDecay(int days) {
        bucketBehaviorEventsData.setLastUpdate(decreaseTodayDateByDays(days));
        Map<BehaviorEventsScoreType, Map<Date, Double>> behaviorEventTypeToOldEventsContributionMap
                = bucketBehaviorEventsData.getBehaviorEventTypeToOldEventsContributionMap();
        Map<BehaviorEventsScoreType, Map<Date, Double>> behaviorEventTypeToOldEventsContributionMapAfterDatesDecayed = new ConcurrentHashMap<>();

        // loop on every behaviorEvent
        for (Map.Entry<BehaviorEventsScoreType, Map<Date, Double>> behaviorEventTypeToOldEventsContributionMapEntry : behaviorEventTypeToOldEventsContributionMap.entrySet()) {
            Map<Date, Double> dayToScoreMap = new ConcurrentHashMap<>();

            // loop on every day
            for (Map.Entry<Date, Double> dateToOldEventsContributionMapEntry : behaviorEventTypeToOldEventsContributionMapEntry.getValue().entrySet()) {
                Date decayedDate = addToDateByDays(dateToOldEventsContributionMapEntry.getKey().getTime(), -days);
                dayToScoreMap.put(decayedDate, dateToOldEventsContributionMapEntry.getValue());
            }

            behaviorEventTypeToOldEventsContributionMapAfterDatesDecayed.put(behaviorEventTypeToOldEventsContributionMapEntry.getKey(), dayToScoreMap);
        }
        bucketBehaviorEventsData.setBehaviorEventTypeToOldEventsContributionMap(behaviorEventTypeToOldEventsContributionMapAfterDatesDecayed);
    }

    private void addBehaviorEvents() {
        BehaviorEventsData behaviorEventsData = new BehaviorEventsData(buildBehaviorEventsDataRequest(BehaviorEventsScoreType.DOUBLE_SPENDING));
        bucketBehaviorEventsService.addEventToCalculations(behaviorEventsData, bucketBehaviorEventsData);

        BehaviorEventsData behaviorEventsData1 = new BehaviorEventsData(buildBehaviorEventsDataRequest(BehaviorEventsScoreType.DOUBLE_SPENDING));
        bucketBehaviorEventsService.addEventToCalculations(behaviorEventsData1, bucketBehaviorEventsData);

        BehaviorEventsData behaviorEventsData2 = new BehaviorEventsData(buildBehaviorEventsDataRequest(BehaviorEventsScoreType.INCORRECT_TRANSACTION));
        bucketBehaviorEventsService.addEventToCalculations(behaviorEventsData2, bucketBehaviorEventsData);

        BehaviorEventsData behaviorEventsData3 = new BehaviorEventsData(buildBehaviorEventsDataRequest(BehaviorEventsScoreType.INCORRECT_TRANSACTION));
        bucketBehaviorEventsService.addEventToCalculations(behaviorEventsData3, bucketBehaviorEventsData);
    }

    private void addFillingTheQuestionnaireEvent(UserType userType) {
        BehaviorEventsData behaviorEventsData = new BehaviorEventsData(buildBehaviorEventsDataRequest(BehaviorEventsScoreType.FILLING_QUESTIONNAIRE));
        bucketBehaviorEventsData.setUserType(userType);
        bucketBehaviorEventsService.addEventToCalculations(behaviorEventsData, bucketBehaviorEventsData);
    }

    private InsertEventRequest buildBehaviorEventsDataRequest(BehaviorEventsScoreType behaviorEventsScoreType) {
        InsertEventRequest insertEventRequest = new InsertEventRequest();
        insertEventRequest.setUserHash(new Hash("1234"));
        insertEventRequest.eventType = EventType.BEHAVIOR_EVENT;
        insertEventRequest.setBehaviorEventsScoreType(behaviorEventsScoreType);
        insertEventRequest.uniqueIdentifier = new Hash("" + ThreadLocalRandom.current().nextLong(10000000, 99999999));
        return insertEventRequest;
    }
}