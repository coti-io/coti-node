import coti.crypto.AlphaNetProofOfWork;

import coti.crypto.IAlgorithm.AlgorithmTypes;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AlphaNetInstantiation {
    @Test
    public void haveAtLeastAnOrder() {
        byte[] startOrder = new byte[0];
        Throwable exception = assertThrows(IllegalArgumentException.class, () -> new AlphaNetProofOfWork(startOrder, 100));
        assertEquals("startOrderSeed must be at least 8 bytes long", exception.getMessage());
    }

    @Test
    public void theRightOrder() {
        byte[] startOrder = new byte[8];
        AlphaNetProofOfWork pow = new AlphaNetProofOfWork(startOrder, 100);
        assertNotNull(pow);
    }

    @Test
    public void correctOrder() {
        byte[] orderSeed = parseHexBinary("0000000000000000");
        AlphaNetProofOfWork pow = new AlphaNetProofOfWork(orderSeed, 0);
        List<AlgorithmTypes> correct = new ArrayList<>();

        // This order is determined by the "seed" which is the input `order`
        correct.add(AlgorithmTypes.SHA512);
        correct.add(AlgorithmTypes.Luffa512);
        correct.add(AlgorithmTypes.Fugue512);
        correct.add(AlgorithmTypes.JH512);
        correct.add(AlgorithmTypes.Hamsi512);
        correct.add(AlgorithmTypes.Shabal512);
        correct.add(AlgorithmTypes.Whirlpool);
        correct.add(AlgorithmTypes.Skein512);
        correct.add(AlgorithmTypes.BLAKE512);
        correct.add(AlgorithmTypes.Keccak512);
        correct.add(AlgorithmTypes.BMW512);
        correct.add(AlgorithmTypes.ECHO512);
        correct.add(AlgorithmTypes.SHAvite512);
        correct.add(AlgorithmTypes.SIMD512);
        correct.add(AlgorithmTypes.Groestl512);
        correct.add(AlgorithmTypes.CubeHash512);

        assertEquals(correct, pow.getHashingAlgorithms());
    }
}
