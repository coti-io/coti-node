package io.coti.pot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProofOfTransactionTest {
    private ProofOfTrust pow;
    private final byte[] targetDifficulty = parseHexBinary("00F00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
    private final byte[] transactionData = "hello world".getBytes();

    @BeforeEach
    public void setUp() {

        pow = new ProofOfTrust(0);
    }

    @Test
    void findNonces() {
        int[] nonces = pow.hash(transactionData, targetDifficulty);
        for (int nonce : nonces) {
            assertTrue(nonce > 0); // making a lil assumption that we won't hit target difficulty on first run
        }
    }

    @Test
    void checkNonceAndDifficulty() {
        int[] nonces = pow.hash(transactionData, targetDifficulty);
        boolean valid = pow.verify(transactionData, nonces, targetDifficulty);
        assertTrue(valid);
    }

    @Test
    void failOnBadNonce() {
        int[] nonces = pow.hash(transactionData, targetDifficulty);
        nonces[0] = nonces[0] + 1;
        boolean valid = pow.verify(transactionData, nonces, targetDifficulty);
        assertFalse(valid);
    }
}
