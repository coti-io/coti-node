package io.coti.pot;

import io.coti.pot.interfaces.IAlgorithm.AlgorithmType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InstantiationTest {


    @Test
    void theRightOrder() {

        ProofOfTrust pow = new ProofOfTrust(100);
        assertNotNull(pow);
    }

    @Test
    void correctOrder() {

        ProofOfTrust pow = new ProofOfTrust(0);
        List<AlgorithmType> correct = new ArrayList<>();

        // This order is determined by the "seed" which is the input `order`
        correct.add(AlgorithmType.SHA_512);
        correct.add(AlgorithmType.LUFFA_512);
        correct.add(AlgorithmType.FUGUE_512);
        correct.add(AlgorithmType.JH_512);
        correct.add(AlgorithmType.HAMSI_512);
        correct.add(AlgorithmType.SHABAL_512);
        correct.add(AlgorithmType.WHIRLPOOL);
        correct.add(AlgorithmType.SKEIN_512_512);
        correct.add(AlgorithmType.BLAKE2B_512);
        correct.add(AlgorithmType.KECCAK_512);
        correct.add(AlgorithmType.BMW_512);
        correct.add(AlgorithmType.ECHO_512);
        correct.add(AlgorithmType.SHA_VITE_512);
        correct.add(AlgorithmType.SIMD_512);
        correct.add(AlgorithmType.GROESTL_512);
        correct.add(AlgorithmType.CUBE_HASH_512);

        assertEquals(correct, pow.getHashingAlgorithms());
    }
}
