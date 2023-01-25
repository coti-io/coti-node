package io.coti.trustscore.services;


import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.trustscore.data.Buckets.BucketInitialTrustScoreEventsData;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Enums.InitialTrustScoreType;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.InitialTrustScoreEventsData;
import io.coti.trustscore.http.InsertEventRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.coti.trustscore.testutils.BucketUtil.generateRulesDataObject;
import static io.coti.trustscore.testutils.GeneralUtilsFunctions.generateRandomHash;
import static io.coti.trustscore.testutils.GeneralUtilsFunctions.isTrustScoreValueValid;
import static io.coti.trustscore.utils.DatesCalculation.decreaseTodayDateByDays;

@TestPropertySource(locations = "classpath:test.properties")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {BucketInitialTrustScoreEventsService.class,
        BaseNodeRocksDBConnector.class
})
class BucketInitialTrustScoreEventsServiceTest {

    @Autowired
    private BucketInitialTrustScoreEventsService bucketInitialTrustScoreEventsService;

    private BucketInitialTrustScoreEventsData bucketInitialTrustScoreEventsData;

    @BeforeEach
    void setUp() {
        bucketInitialTrustScoreEventsService.init(generateRulesDataObject());
        bucketInitialTrustScoreEventsData = new BucketInitialTrustScoreEventsData();
        bucketInitialTrustScoreEventsData.setUserType(UserType.MERCHANT);
    }

    @Test
    void addEventToCalculationsSimpleTest() {
        addInitialTrustScoreEvents();
        double bucketSumScore = bucketInitialTrustScoreEventsService.getBucketSumScore(bucketInitialTrustScoreEventsData);
        Assertions.assertTrue(isTrustScoreValueValid(bucketSumScore));
    }

    @Test
    void addEventToCalculationsWithDecayTest() {
        addInitialTrustScoreEvents();
        bucketInitialTrustScoreEventsData.setLastUpdate(decreaseTodayDateByDays(1));


        InitialTrustScoreEventsData initialTrustScoreEventsData
                = new InitialTrustScoreEventsData(buildInitialTrustScoreEventsDataRequest(InitialTrustScoreType.MERCHANT_QUESTIONNAIRE, 10.5));
        bucketInitialTrustScoreEventsService.addEventToCalculations(initialTrustScoreEventsData, bucketInitialTrustScoreEventsData);
        bucketInitialTrustScoreEventsService.getBucketSumScore(bucketInitialTrustScoreEventsData);

        bucketInitialTrustScoreEventsData.setLastUpdate(decreaseTodayDateByDays(2));
        double bucketSumScore = bucketInitialTrustScoreEventsService.getBucketSumScore(bucketInitialTrustScoreEventsData);
        Assertions.assertTrue(isTrustScoreValueValid(bucketSumScore));
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
        insertEventRequest.setEventType(EventType.INITIAL_EVENT);
        insertEventRequest.setScore(chargeBackAmount);
        insertEventRequest.setInitialTrustScoreType(initialTrustScoreType);
        insertEventRequest.setUniqueIdentifier(generateRandomHash(72));
        return insertEventRequest;
    }
}
