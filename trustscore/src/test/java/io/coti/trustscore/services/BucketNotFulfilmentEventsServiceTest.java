package io.coti.trustscore.services;

import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.trustscore.data.buckets.BucketNotFulfilmentEventsData;
import io.coti.trustscore.data.enums.CompensableEventScoreType;
import io.coti.trustscore.data.enums.EventType;
import io.coti.trustscore.data.enums.UserType;
import io.coti.trustscore.data.events.NotFulfilmentEventsData;
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

import static io.coti.trustscore.testutils.BucketUtil.generateRulesDataObject;
import static io.coti.trustscore.testutils.GeneralUtilsFunctions.generateRandomHash;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {BucketNotFulfilmentEventsService.class,
        BaseNodeRocksDBConnector.class
})
public class BucketNotFulfilmentEventsServiceTest {

    @Autowired
    private BucketNotFulfilmentEventsService bucketNotFulfilmentEventsService;

    private BucketNotFulfilmentEventsData bucketNotFulfilmentEventsData;

    @Before
    public void setUp() {
        bucketNotFulfilmentEventsService.init(generateRulesDataObject());
        bucketNotFulfilmentEventsData = new BucketNotFulfilmentEventsData();
        bucketNotFulfilmentEventsData.setUserType(UserType.MERCHANT);
    }

    @Test
    public void addEventToCalculations() {
        NotFulfilmentEventsData notFulfilmentEventsData
                = new NotFulfilmentEventsData(buildBehaviorEventsDataRequest(CompensableEventScoreType.NON_FULFILMENT));
        bucketNotFulfilmentEventsService.addEventToCalculations(notFulfilmentEventsData, bucketNotFulfilmentEventsData);
        double bucketSumScore = bucketNotFulfilmentEventsService.getBucketSumScore(bucketNotFulfilmentEventsData);
        Assert.assertTrue(bucketSumScore < 0);
    }

    @Test
    public void getBucketEventType() {
        EventType eventType = bucketNotFulfilmentEventsService.getBucketEventType();
        Assert.assertTrue(eventType == EventType.NOT_FULFILMENT_EVENT);
    }

    private InsertEventRequest buildBehaviorEventsDataRequest(CompensableEventScoreType compensableEventScoreType) {
        InsertEventRequest insertEventRequest = new InsertEventRequest();
        insertEventRequest.setUserHash(generateRandomHash(64));
        insertEventRequest.eventType = EventType.NOT_FULFILMENT_EVENT;
        insertEventRequest.setCompensableEventScoreType(compensableEventScoreType);
        insertEventRequest.uniqueIdentifier = generateRandomHash(64);
        insertEventRequest.setDebtAmount(10000);
        insertEventRequest.setOtherUserHash(generateRandomHash(64));
        return insertEventRequest;
    }
}