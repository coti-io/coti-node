package io.coti.fullnode.services;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Duration;
import java.time.Instant;

import static testUtils.TestUtils.generateRandomLongNumber;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
public class MonitorBucketStatisticsTest {

    @Test
    public void getAverage() {
        MonitorBucketStatistics monitorBucketStatistics = new MonitorBucketStatistics();
        monitorBucketStatistics.setNumberOfTransaction(generateRandomLongNumber());

        double average = monitorBucketStatistics.getAverage();

        Assert.assertEquals(0, average, 0.0);
    }

    @Test
    public void addTransactionStatistics_noExceptionIsThrown() {
        try {
            MonitorBucketStatistics monitorBucketStatistics = new MonitorBucketStatistics();
            monitorBucketStatistics.setNumberOfTransaction(generateRandomLongNumber());

            monitorBucketStatistics.addTransactionStatistics(Duration.between(Instant.now()
                    , Instant.now().plusNanos(generateRandomLongNumber())));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}