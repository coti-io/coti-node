package io.coti.trustscore.services;


import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.trustscore.data.Buckets.BucketInitialTrustScoreEventsData;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Enums.InitialTrustScoreType;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.InitialTrustScoreEventsData;
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

import static io.coti.trustscore.testUtils.BucketUtil.generateRulesDataObject;
import static io.coti.trustscore.testUtils.GeneralUtilsFunctions.generateRandomHash;
import static io.coti.trustscore.testUtils.GeneralUtilsFunctions.isTrustScoreValueValid;
import static io.coti.trustscore.utils.DatesCalculation.decreaseTodayDateByDays;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {BucketInitialTrustScoreEventsService.class,
        BaseNodeRocksDBConnector.class
})
public class BucketInitialTrustScoreEventsServiceTest {

    @Autowired
    private BucketInitialTrustScoreEventsService bucketInitialTrustScoreEventsService;

    private BucketInitialTrustScoreEventsData bucketInitialTrustScoreEventsData;

    @Before
    public void setUp() {
        bucketInitialTrustScoreEventsService.init(generateRulesDataObject());
        bucketInitialTrustScoreEventsData = new BucketInitialTrustScoreEventsData();
        bucketInitialTrustScoreEventsData.setUserType(UserType.MERCHANT);
    }

    @Test
    public void addEventToCalculations_simpleTest() {
        addInitialTrustScoreEvents();
        double bucketSumScore = bucketInitialTrustScoreEventsService.getBucketSumScore(bucketInitialTrustScoreEventsData);
        isTrustScoreValueValid(bucketSumScore);
    }

    @Test
    public void addEventToCalculations_WithDecay_Test() {
        addInitialTrustScoreEvents();
        bucketInitialTrustScoreEventsData.setLastUpdate(decreaseTodayDateByDays(1));


        InitialTrustScoreEventsData initialTrustScoreEventsData
                = new InitialTrustScoreEventsData(buildInitialTrustScoreEventsDataRequest(InitialTrustScoreType.MERCHANT_QUESTIONNAIRE, 10.5));
        bucketInitialTrustScoreEventsService.addEventToCalculations(initialTrustScoreEventsData, bucketInitialTrustScoreEventsData);
        bucketInitialTrustScoreEventsService.getBucketSumScore(bucketInitialTrustScoreEventsData);

        bucketInitialTrustScoreEventsData.setLastUpdate(decreaseTodayDateByDays(2));
        double bucketSumScore = bucketInitialTrustScoreEventsService.getBucketSumScore(bucketInitialTrustScoreEventsData);
        Assert.assertTrue(isTrustScoreValueValid(bucketSumScore));
    }

    private void addInitialTrustScoreEvents() {
        InitialTrustScoreEventsData initialTrustScoreEventsData
                = new InitialTrustScoreEventsData(buildInitialTrustScoreEventsDataRequest(InitialTrustScoreType.GENERAL_QUESTIONNAIRE, 15.6));
        bucketInitialTrustScoreEventsService.addEventToCalculations(initialTrustScoreEventsData, bucketInitialTrustScoreEventsData);

        initialTrustScoreEventsData
                = new InitialTrustScoreEventsData(buildInitialTrustScoreEventsDataRequest(InitialTrustScoreType.MERCHANT_QUESTIONNAIRE, 12.3));
        bucketInitialTrustScoreEventsService.addEventToCalculations(initialTrustScoreEventsData, bucketInitialTrustScoreEventsData);


        initialTrustScoreEventsData
                = new InitialTrustScoreEventsData(buildInitialTrustScoreEventsDataRequest(InitialTrustScoreType.GENERAL_QUESTIONNAIRE, 10));
        bucketInitialTrustScoreEventsService.addEventToCalculations(initialTrustScoreEventsData, bucketInitialTrustScoreEventsData);
    }

    private InsertEventRequest buildInitialTrustScoreEventsDataRequest(InitialTrustScoreType initialTrustScoreType, double chargeBackAmount) {
        InsertEventRequest insertEventRequest = new InsertEventRequest();
        insertEventRequest.setUserHash(generateRandomHash(64));
        insertEventRequest.eventType = EventType.INITIAL_EVENT;
        insertEventRequest.setScore(chargeBackAmount);
        insertEventRequest.setInitialTrustScoreType(initialTrustScoreType);
        insertEventRequest.uniqueIdentifier = generateRandomHash(72);
        return insertEventRequest;
    }
}