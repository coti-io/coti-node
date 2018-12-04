package io.coti.trustscore.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.database.RocksDBConnector;
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

import java.util.concurrent.ThreadLocalRandom;

import static io.coti.trustscore.BucketUtil.generateRulesDataObject;
import static io.coti.trustscore.utils.MathCalculation.ifTwoNumbersAreEqualOrAlmostEqual;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = {BucketNotFulfilmentEventsService.class,
        RocksDBConnector.class
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
        Assert.assertTrue(ifTwoNumbersAreEqualOrAlmostEqual(bucketSumScore, -0.08219178));
    }

    @Test
    public void getBucketEventType() {
        EventType eventType = bucketNotFulfilmentEventsService.getBucketEventType();
        Assert.assertTrue(eventType == EventType.NOT_FULFILMENT_EVENT);
    }

    private InsertEventRequest buildBehaviorEventsDataRequest(CompensableEventScoreType compensableEventScoreType) {
        InsertEventRequest insertEventRequest = new InsertEventRequest();
        insertEventRequest.setUserHash(new Hash("1234"));
        insertEventRequest.eventType = EventType.NOT_FULFILMENT_EVENT;
        insertEventRequest.setCompensableEventScoreType(compensableEventScoreType);
        insertEventRequest.uniqueIdentifier = new Hash("" + ThreadLocalRandom.current().nextLong(10000000, 99999999));
        insertEventRequest.setDebtAmount(10000);
        insertEventRequest.setOtherUserHash(new Hash("4567"));
        return insertEventRequest;
    }
}