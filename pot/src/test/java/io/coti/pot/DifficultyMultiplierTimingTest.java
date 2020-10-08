package io.coti.pot;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;
import static javax.xml.bind.DatatypeConverter.printHexBinary;
import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DifficultyMultiplierTimingTest {

    private static final int sampleSize = 200;
    private static final int difficultyMultiplier = 2;

    @Test
    void difficultyMultiplierTiming() {
        List<TrustScoreDifficulty> points = new ArrayList<>();
        points.add(new TrustScoreDifficulty(6, "00780000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));
        points.add(new TrustScoreDifficulty(12, "001E0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));
        points.add(new TrustScoreDifficulty(18, "001E0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));
        points.add(new TrustScoreDifficulty(25, "001E0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));
        points.add(new TrustScoreDifficulty(31, "000F0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));
        points.add(new TrustScoreDifficulty(37, "000F0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));
        points.add(new TrustScoreDifficulty(43, "000F0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));
        points.add(new TrustScoreDifficulty(50, "000F0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));
        points.add(new TrustScoreDifficulty(56, "000F0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));
        points.add(new TrustScoreDifficulty(62, "00078000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));
        points.add(new TrustScoreDifficulty(68, "00078000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));
        points.add(new TrustScoreDifficulty(75, "00078000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));
        points.add(new TrustScoreDifficulty(81, "0003C000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));
        points.add(new TrustScoreDifficulty(87, "0003C000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));
        points.add(new TrustScoreDifficulty(93, "0003C000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));
        points.add(new TrustScoreDifficulty(100, "0003C000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000"));

        for (TrustScoreDifficulty pair : points) {
            assertDoesNotThrow(() -> batchRun(pair.getDifficulty(), pair.getTrustScore(), sampleSize, difficultyMultiplier));
        }
    }

    public void batchRun(String easierHexDifficulty, int trustScore, int sampleSize, int difficultyMultiplier) {
        byte[] easierDifficulty = parseHexBinary(easierHexDifficulty);
        byte[] harderDifficulty = new BigInteger(easierDifficulty).divide(new BigInteger(String.valueOf(difficultyMultiplier))).toByteArray();
        String harderHexDifficulty = leftPad(printHexBinary(harderDifficulty), 128, "0");

        System.out.println("Trust score: " + trustScore);
        System.out.println("Sample size: " + sampleSize);
        System.out.println("Difficulty multiplier: " + difficultyMultiplier);
        System.out.println("Easier difficulty: 0x" + easierHexDifficulty);
        System.out.println("Harder difficulty: 0x" + harderHexDifficulty);

        BenchmarkBatching easierBatching = new BenchmarkBatching(sampleSize, easierDifficulty, trustScore);
        double easierMedian = easierBatching.getMedian();
        double easierMean = easierBatching.getMean();
        System.out.println("Median time for easier difficulty: " + easierMedian + "s");
        System.out.println("Mean time for easier difficulty: " + easierMean + "s");

        BenchmarkBatching harderBatching = new BenchmarkBatching(sampleSize, harderDifficulty, trustScore);
        double harderMedian = harderBatching.getMedian();
        double harderMean = harderBatching.getMean();
        System.out.println("Median time for harder difficulty: " + harderMedian + "s");
        System.out.println("Mean time for harder difficulty: " + harderMean + "s");

        double rateDifferenceMedian = harderMedian / easierMedian;
        double normalizedDifferenceMedian = rateDifferenceMedian / difficultyMultiplier;

        double rateDifferenceMean = harderMean / easierMean;
        double normalizedDifferenceMean = rateDifferenceMean / difficultyMultiplier;

        System.out.println("Median rate difference (harder median / easier median) (should match difficulty multiplier): " + rateDifferenceMedian);
        System.out.println("Normalized median difference (rate difference median / difficulty multiplier) (should be close to 1): " + normalizedDifferenceMedian);

        System.out.println("Mean rate difference (harder mean / easier mean) (should match difficulty multiplier): " + rateDifferenceMean);
        System.out.println("Normalized mean difference (rate difference mean / difficulty multiplier) (should be close to 1): " + normalizedDifferenceMean);

        System.out.println();
        System.out.println("----------------------------------------------------------------------------------------------------------");
        System.out.println();
    }
}
