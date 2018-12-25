package io.coti.fullnode.services;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;
import static testUtils.TestUtils.generateRandomCount;
import static testUtils.TestUtils.generateRandomLongNumber;

@TestPropertySource(locations = "classpath:test.properties")
@RunWith(SpringRunner.class)
public class MonitorBucketStatisticsTest {

    @Test
    public void getAverage() {
        MonitorBucketStatistics monitorBucketStatistics = new  MonitorBucketStatistics();
        monitorBucketStatistics.setNumberOfTransaction(generateRandomLongNumber());

        double average = monitorBucketStatistics.getAverage();

        Assert.assertTrue(average == 0);
    }

    @Test
    public void addTransactionStatistics() {
    }
}