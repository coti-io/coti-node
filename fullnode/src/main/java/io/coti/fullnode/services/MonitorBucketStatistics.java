package io.coti.fullnode.services;

import lombok.Data;

import java.time.Duration;

@Data
public class MonitorBucketStatistics {

    public Duration totalTime;
    public double numberOfTransaction;

    public MonitorBucketStatistics() {
        this.totalTime = Duration.ZERO;
        this.numberOfTransaction = 0;
    }

    public double getAverage() {
        return (totalTime.toNanos() / numberOfTransaction) / 1000000;
    }

    public synchronized void addTransactionStatistics(Duration time) {
        totalTime = totalTime.plusNanos(time.toNanos());
        numberOfTransaction = numberOfTransaction + 1;
    }
}
