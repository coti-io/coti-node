import io.coti.pot.interfaces.IAlgorithm;
import org.junit.jupiter.api.Test;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;

public class TrustScoreTiming {

    @Test
    public void trustScoreTimingComparisons() {
        int[] sampleSizes = new int[]{
                10,
                100,
                1000,
                10000
        };

        String[] difficulties = new String[]{
                "00500000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                "00050000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                "00005000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
        };

        for (int size : sampleSizes) {
            for (String difficulty : difficulties) {
                batchRun(size, difficulty);
            }
        }
    }

    public void batchRun(int sampleSize, String difficulty) {
        System.out.println("difficulty: 0x" + difficulty);
        System.out.println("sample size: " + sampleSize);

        System.out.println();
        System.out.println("----------------------------------------------------------------------------------------------------------");
        System.out.println();

        for (int i = IAlgorithm.AlgorithmTypes.values().length - 1; i >= 0; i--) {
            int trustScore = TrustScoreDifficulty.getTrustScoreFromSegment(i);
            byte[] diff = parseHexBinary(difficulty);
            System.out.println("trust score: " + trustScore);
            BenchmarkBatching batching = new BenchmarkBatching(sampleSize, diff, trustScore);
            double median = batching.getMedian();
            double mean = batching.getMean();
            System.out.println("median execution time: " + median + "s");
            System.out.println("mean execution time: " + mean + "s");
            System.out.println();
        }

        System.out.println("----------------------------------------------------------------------------------------------------------");
        System.out.println("----------------------------------------------------------------------------------------------------------");
        System.out.println();
    }
}
