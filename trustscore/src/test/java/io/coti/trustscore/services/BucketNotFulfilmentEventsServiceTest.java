package io.coti.trustscore.services;

import io.coti.basenode.database.BaseNodeRocksDBConnector;
import io.coti.trustscore.data.Buckets.BucketNotFulfilmentEventsData;
import io.coti.trustscore.data.Enums.CompensableEventScoreType;
import io.coti.trustscore.data.Enums.EventType;
import io.coti.trustscore.data.Enums.UserType;
import io.coti.trustscore.data.Events.NotFulfilmentEventsData;
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
        Assert.assertEquals(EventType.NOT_FULFILMENT_EVENT, eventType);
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
