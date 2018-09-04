import io.coti.pot.ProofOfTrust;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProofOfTransactionTest {
    private ProofOfTrust pow;
    private byte[] targetDifficulty = parseHexBinary("00F00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000");
    private byte[] transactionData = "hello world".getBytes();

    @BeforeEach
    public void setup() {

        pow = new ProofOfTrust(0);
    }

    @Test
    public void findNonces() {
        int[] nonces = pow.hash(transactionData, targetDifficulty);
        for (int nonce : nonces) {
            assertTrue(nonce > 0); // making a lil assumption that we won't hit target difficulty on first run
        }
    }

    @Test
    public void checkNonceAndDifficulty() {
        int[] nonces = pow.hash(transactionData, targetDifficulty);
        boolean valid = pow.verify(transactionData, nonces, targetDifficulty);
        assertTrue(valid);
    }

    @Test
    public void failOnBadNonce() {
        int[] nonces = pow.hash(transactionData, targetDifficulty);
        nonces[0] = nonces[0] + 1;
        boolean valid = pow.verify(transactionData, nonces, targetDifficulty);
        assertFalse(valid);
    }
}
