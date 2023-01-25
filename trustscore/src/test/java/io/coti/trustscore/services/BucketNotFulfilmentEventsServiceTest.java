package io.coti.trustscore.services;

import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.trustscore.data.Buckets.BucketNotFulfilmentEventsData;
import io.coti.trustscore.data.Enums.CompensableEventScoreType;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.NotFulfilmentEventsData;
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

@TestPropertySource(locations = "classpath:test.properties")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = {BucketNotFulfilmentEventsService.class,
        BaseNodeRocksDBConnector.class
})
class BucketNotFulfilmentEventsServiceTest {

    @Autowired
    private BucketNotFulfilmentEventsService bucketNotFulfilmentEventsService;

    private BucketNotFulfilmentEventsData bucketNotFulfilmentEventsData;

    @BeforeEach
    public void setUp() {
        bucketNotFulfilmentEventsService.init(generateRulesDataObject());
        bucketNotFulfilmentEventsData = new BucketNotFulfilmentEventsData();
        bucketNotFulfilmentEventsData.setUserType(UserType.MERCHANT);
    }

    @Test
    void addEventToCalculations() {
        NotFulfilmentEventsData notFulfilmentEventsData
                = new NotFulfilmentEventsData(buildBehaviorEventsDataRequest(CompensableEventScoreType.NON_FULFILMENT));
        bucketNotFulfilmentEventsService.addEventToCalculations(notFulfilmentEventsData, bucketNotFulfilmentEventsData);
        double bucketSumScore = bucketNotFulfilmentEventsService.getBucketSumScore(bucketNotFulfilmentEventsData);
        Assertions.assertTrue(bucketSumScore < 0);
    }

    @Test
    void getBucketEventType() {
        EventType eventType = bucketNotFulfilmentEventsService.getBucketEventType();
        Assertions.assertEquals(EventType.NOT_FULFILMENT_EVENT, eventType);
    }

    private InsertEventRequest buildBehaviorEventsDataRequest(CompensableEventScoreType compensableEventScoreType) {
        InsertEventRequest insertEventRequest = new InsertEventRequest();
        insertEventRequest.setUserHash(generateRandomHash(64));
        insertEventRequest.setEventType(EventType.NOT_FULFILMENT_EVENT);
        insertEventRequest.setCompensableEventScoreType(compensableEventScoreType);
        insertEventRequest.setUniqueIdentifier(generateRandomHash(64));
        insertEventRequest.setDebtAmount(10000);
        insertEventRequest.setOtherUserHash(generateRandomHash(64));
        return insertEventRequest;
    }
}
