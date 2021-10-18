package io.coti.pot;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.Random;

public class BenchmarkBatching {

    private final double _median;
    private final double _mean;

    public BenchmarkBatching(int sampleSize, byte[] targetDifficulty, int trustScore) {
        Random rand = new Random();

        byte[][] orderData = new byte[sampleSize][8];
        byte[][] inputData = new byte[sampleSize][512];

        for (int i = 0; i < sampleSize; i++) {
            rand.nextBytes(orderData[i]);
            rand.nextBytes(inputData[i]);
        }

        long[] durations = new long[sampleSize];

        for (int i = 0; i < sampleSize; i++) {
            long startTime = getCpuTime();

            ProofOfTrust pow = new ProofOfTrust(trustScore);
            pow.hash(inputData[i], targetDifficulty);

            long endTime = getCpuTime();
            long duration = (endTime - startTime);

            durations[i] = duration;
        }

        _mean = Arrays.stream(durations).average().getAsDouble() / 1000000000.0;

        Arrays.sort(durations);
        double median;
        if (durations.length % 2 == 0) {
            median = ((double) durations[durations.length / 2] + (double) durations[durations.length / 2 - 1]) / 2;
        } else {
            median = (double) durations[durations.length / 2];
        }

        _median = median / 1000000000.0;
    }

    public double getMedian() {
        return _median;
    }

    public double getMean() {
        return _mean;
    }

    /**
     * Get CPU time in nanoseconds.
     */
    public static long getCpuTime() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        return bean.isCurrentThreadCpuTimeSupported() ?
                bean.getCurrentThreadCpuTime() : 0L;
    }
}
