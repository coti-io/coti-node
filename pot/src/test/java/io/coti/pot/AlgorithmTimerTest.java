package io.coti.pot;

import io.coti.pot.interfaces.IAlgorithm;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AlgorithmTimerTest {

    private static final int SAMPLE_SIZE = 1000000;
    private static final byte[][] inputData = new byte[SAMPLE_SIZE][512];
    private static final Map<IAlgorithm.AlgorithmType, Double> results = new HashMap<>();

    @BeforeAll
    public static void setUp() {
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            inputData[i] = ByteBuffer.allocate(4).putInt(i).array();
        }
    }

    @AfterAll
    public static void compare() {
        List<Map.Entry<IAlgorithm.AlgorithmType, Double>> entries = new ArrayList<>(results.entrySet());
        entries.sort(Map.Entry.comparingByValue());

        System.out.println("Sorted results: ");
        for (Map.Entry<IAlgorithm.AlgorithmType, Double> entry : entries) {
            System.out.println(entry.getKey() + " - " + entry.getValue());
        }
    }

    @Test
    void skein() {
        IAlgorithm.AlgorithmType type = IAlgorithm.AlgorithmType.SKEIN_512_512;
        assertDoesNotThrow(() -> results.put(type, doWork(type)));
    }

    @Test
    void bmw() {
        IAlgorithm.AlgorithmType type = IAlgorithm.AlgorithmType.BMW_512;
        assertDoesNotThrow(() -> results.put(type, doWork(type)));
    }

    @Test
    void blake() {
        IAlgorithm.AlgorithmType type = IAlgorithm.AlgorithmType.BLAKE2B_512;
        assertDoesNotThrow(() -> results.put(type, doWork(type)));
    }

    @Test
    void sha() {
        IAlgorithm.AlgorithmType type = IAlgorithm.AlgorithmType.SHA_512;
        assertDoesNotThrow(() -> results.put(type, doWork(type)));
    }

    @Test
    void keccak() {
        IAlgorithm.AlgorithmType type = IAlgorithm.AlgorithmType.KECCAK_512;
        assertDoesNotThrow(() -> results.put(type, doWork(type)));
    }

    @Test
    void shabal() {
        IAlgorithm.AlgorithmType type = IAlgorithm.AlgorithmType.SHABAL_512;
        assertDoesNotThrow(() -> results.put(type, doWork(type)));
    }

    @Test
    void shavite() {
        IAlgorithm.AlgorithmType type = IAlgorithm.AlgorithmType.SHA_VITE_512;
        assertDoesNotThrow(() -> results.put(type, doWork(type)));
    }

    @Test
    void whirlpool() {
        IAlgorithm.AlgorithmType type = IAlgorithm.AlgorithmType.WHIRLPOOL;
        assertDoesNotThrow(() -> results.put(type, doWork(type)));
    }

    @Test
    void cubehash() {
        IAlgorithm.AlgorithmType type = IAlgorithm.AlgorithmType.CUBE_HASH_512;
        assertDoesNotThrow(() -> results.put(type, doWork(type)));
    }

    @Test
    void jh() {
        IAlgorithm.AlgorithmType type = IAlgorithm.AlgorithmType.JH_512;
        assertDoesNotThrow(() -> results.put(type, doWork(type)));
    }

    @Test
    void luffa() {
        IAlgorithm.AlgorithmType type = IAlgorithm.AlgorithmType.LUFFA_512;
        assertDoesNotThrow(() -> results.put(type, doWork(type)));
    }

    @Test
    void echo() {
        IAlgorithm.AlgorithmType type = IAlgorithm.AlgorithmType.ECHO_512;
        assertDoesNotThrow(() -> results.put(type, doWork(type)));
    }

    @Test
    void simd() {
        IAlgorithm.AlgorithmType type = IAlgorithm.AlgorithmType.SIMD_512;
        assertDoesNotThrow(() -> results.put(type, doWork(type)));
    }

    @Test
    void fugue() {
        IAlgorithm.AlgorithmType type = IAlgorithm.AlgorithmType.FUGUE_512;
        assertDoesNotThrow(() -> results.put(type, doWork(type)));
    }

    @Test
    void groestl() {
        IAlgorithm.AlgorithmType type = IAlgorithm.AlgorithmType.GROESTL_512;
        assertDoesNotThrow(() -> results.put(type, doWork(type)));
    }

    @Test
    void hamsi() {
        IAlgorithm.AlgorithmType type = IAlgorithm.AlgorithmType.HAMSI_512;
        assertDoesNotThrow(() -> results.put(type, doWork(type)));
    }

    private double doWork(IAlgorithm.AlgorithmType type) {
        double durationRound;
        Algorithm algo = new Algorithm(type);
        long startTimeRound = BenchmarkBatching.getCpuTime();

        for (int i = 0; i < SAMPLE_SIZE; i++) {
            algo.hash(inputData[i]);
        }

        long endTimeRound = BenchmarkBatching.getCpuTime();
        durationRound = (endTimeRound - startTimeRound) / 1000000000.0;

        System.out.println("Time for all " + SAMPLE_SIZE + " " + type + " runs to complete:");
        System.out.println("  " + durationRound + "s");
        System.out.println();

        return durationRound;
    }
}
