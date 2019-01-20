package io.coti.trustscore.services;

import io.coti.basenode.database.BaseNodeRocksDBConnector;
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

import static io.coti.trustscore.testUtils.BucketUtil.generateRulesDataObject;
import static io.coti.trustscore.testUtils.GeneralUtilsFunctions.generateRandomHash;
import static io.coti.trustscore.utils.DatesCalculation.addToDateByDays;
import static io.coti.trustscore.utils.DatesCalculation.decreaseTodayDateByDays;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {BucketBehaviorEventsService.class,
        BaseNodeRocksDBConnector.class
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
        performSimulationOfDecay(3);
        performSimulationOfDecay(2);

        BehaviorEventsData behaviorEventsData = new BehaviorEventsData(buildBehaviorEventsDataRequest(BehaviorEventsScoreType.INCORRECT_TRANSACTION));
        bucketBehaviorEventsService.addEventToCalculations(behaviorEventsData, bucketBehaviorEventsData);
        performSimulationOfDecay(2);

        BehaviorEventsData behaviorEventsData1 = new BehaviorEventsData(buildBehaviorEventsDataRequest(BehaviorEventsScoreType.INCORRECT_TRANSACTION));
        bucketBehaviorEventsService.addEventToCalculations(behaviorEventsData1, bucketBehaviorEventsData);
        BehaviorEventsData behaviorEventsData2 = new BehaviorEventsData(buildBehaviorEventsDataRequest(BehaviorEventsScoreType.DOUBLE_SPENDING));
        bucketBehaviorEventsService.addEventToCalculations(behaviorEventsData2, bucketBehaviorEventsData);
        for (int i = 0; i < 5; i++) {
            addFillingTheQuestionnaireEvent(UserType.MERCHANT);
        }
        performSimulationOfDecay(2);

        double bucketSumScore = bucketBehaviorEventsService.getBucketSumScore(bucketBehaviorEventsData);
        Assert.assertTrue(bucketSumScore < 0);
    }

    @Test
    public void BucketBehaviorEventsService_simpleScenarioTest() {
        addBehaviorEvents();

        for (int i = 0; i < 8; i++) {
            addFillingTheQuestionnaireEvent(UserType.MERCHANT);
        }

        for (int i = 0; i < 6; i++) {
            addFillingTheQuestionnaireEvent(UserType.MERCHANT);
        }
        double bucketSumScore = bucketBehaviorEventsService.getBucketSumScore(bucketBehaviorEventsData);
        Assert.assertTrue(bucketSumScore < 0);
    }

    @Test
    public void BucketBehaviorEventsService_withLargeDecayTest() {
        addBehaviorEvents();
        performSimulationOfDecay(400);
        addBehaviorEvents();
        performSimulationOfDecay(1);
        bucketBehaviorEventsService.getBucketSumScore(bucketBehaviorEventsData);
        addBehaviorEvents();
        bucketBehaviorEventsService.getBucketSumScore(bucketBehaviorEventsData);
        performSimulationOfDecay(20);
        performSimulationOfDecay(350);
        double bucketSumScore = bucketBehaviorEventsService.getBucketSumScore(bucketBehaviorEventsData);
        Assert.assertTrue(bucketSumScore < 0);
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
        insertEventRequest.setUserHash(generateRandomHash(64));
        insertEventRequest.eventType = EventType.BEHAVIOR_EVENT;
        insertEventRequest.setBehaviorEventsScoreType(behaviorEventsScoreType);
        insertEventRequest.uniqueIdentifier = generateRandomHash(72);
        return insertEventRequest;
    }
}