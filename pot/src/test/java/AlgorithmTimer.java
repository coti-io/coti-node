import io.coti.pot.Algorithm;
import io.coti.pot.interfaces.IAlgorithm;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class AlgorithmTimer {
    private static int sampleSize = 1000000;
    private static byte[][] inputData = new byte[sampleSize][512];

    private static Map<IAlgorithm.AlgorithmTypes, Double> results = new HashMap<>();

    @BeforeAll
    static void setup() {
        for (int i = 0; i < sampleSize; i++) {
            inputData[i] = ByteBuffer.allocate(4).putInt(i).array();
        }
    }

    @AfterAll
    static void compare() {
        List<Map.Entry<IAlgorithm.AlgorithmTypes, Double>> entries = new ArrayList<>(results.entrySet());
        entries.sort(Map.Entry.comparingByValue());

        System.out.println("sorted results: ");
        for (Map.Entry<IAlgorithm.AlgorithmTypes, Double> entry : entries) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
    }

    @Test
    void skein() {
        IAlgorithm.AlgorithmTypes type = IAlgorithm.AlgorithmTypes.Skein_512_512;
        results.put(type, dowork(type));
    }

    @Test
    void bmw() {
        IAlgorithm.AlgorithmTypes type = IAlgorithm.AlgorithmTypes.BMW512;
        results.put(type, dowork(type));
    }

    @Test
    void blake() {
        IAlgorithm.AlgorithmTypes type = IAlgorithm.AlgorithmTypes.BLAKE2B_512;
        results.put(type, dowork(type));
    }

    @Test
    void sha() {
        IAlgorithm.AlgorithmTypes type = IAlgorithm.AlgorithmTypes.SHA_512;
        results.put(type, dowork(type));
    }

    @Test
    void keccak() {
        IAlgorithm.AlgorithmTypes type = IAlgorithm.AlgorithmTypes.KECCAK_512;
        results.put(type, dowork(type));
    }

    @Test
    void shabal() {
        IAlgorithm.AlgorithmTypes type = IAlgorithm.AlgorithmTypes.Shabal512;
        results.put(type, dowork(type));
    }

    @Test
    void shavite() {
        IAlgorithm.AlgorithmTypes type = IAlgorithm.AlgorithmTypes.SHAvite512;
        results.put(type, dowork(type));
    }

    @Test
    void whirlpool() {
        IAlgorithm.AlgorithmTypes type = IAlgorithm.AlgorithmTypes.WHIRLPOOL;
        results.put(type, dowork(type));
    }

    @Test
    void cubehash() {
        IAlgorithm.AlgorithmTypes type = IAlgorithm.AlgorithmTypes.CubeHash512;
        results.put(type, dowork(type));
    }

    @Test
    void jh() {
        IAlgorithm.AlgorithmTypes type = IAlgorithm.AlgorithmTypes.JH512;
        results.put(type, dowork(type));
    }

    @Test
    void luffa() {
        IAlgorithm.AlgorithmTypes type = IAlgorithm.AlgorithmTypes.Luffa512;
        results.put(type, dowork(type));
    }

    @Test
    void echo() {
        IAlgorithm.AlgorithmTypes type = IAlgorithm.AlgorithmTypes.ECHO512;
        results.put(type, dowork(type));
    }

    @Test
    void simd() {
        IAlgorithm.AlgorithmTypes type = IAlgorithm.AlgorithmTypes.SIMD512;
        results.put(type, dowork(type));
    }

    @Test
    void fugue() {
        IAlgorithm.AlgorithmTypes type = IAlgorithm.AlgorithmTypes.Fugue512;
        results.put(type, dowork(type));
    }

    @Test
    void groestl() {
        IAlgorithm.AlgorithmTypes type = IAlgorithm.AlgorithmTypes.Groestl512;
        results.put(type, dowork(type));
    }

    @Test
    void hamsi() {
        IAlgorithm.AlgorithmTypes type = IAlgorithm.AlgorithmTypes.Hamsi512;
        results.put(type, dowork(type));
    }

    private double dowork(IAlgorithm.AlgorithmTypes type) {
        double durationRound;
        Algorithm algo = new Algorithm(type);
        long startTimeRound = BenchmarkBatching.getCpuTime();

        for (int i = 0; i < sampleSize; i++) {
            algo.hash(inputData[i]);
        }

        long endTimeRound = BenchmarkBatching.getCpuTime();
        durationRound = (endTimeRound - startTimeRound) / 1000000000.0;

        System.out.println("time for all " + sampleSize + " " + type + " runs to complete:");
        System.out.println("  " + durationRound + "s");
        System.out.println();

        return durationRound;
    }
}
