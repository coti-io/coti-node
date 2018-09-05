import io.coti.pot.ProofOfTrust;
import io.coti.pot.interfaces.IAlgorithm.AlgorithmTypes;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Instantiation {


    @Test
    public void theRightOrder() {

        ProofOfTrust pow = new ProofOfTrust(100);
        assertNotNull(pow);
    }

    @Test
    public void correctOrder() {

        ProofOfTrust pow = new ProofOfTrust(0);
        List<AlgorithmTypes> correct = new ArrayList<>();

        // This order is determined by the "seed" which is the input `order`
        correct.add(AlgorithmTypes.SHA_512);
        correct.add(AlgorithmTypes.Luffa512);
        correct.add(AlgorithmTypes.Fugue512);
        correct.add(AlgorithmTypes.JH512);
        correct.add(AlgorithmTypes.Hamsi512);
        correct.add(AlgorithmTypes.Shabal512);
        correct.add(AlgorithmTypes.WHIRLPOOL);
        correct.add(AlgorithmTypes.Skein_512_512);
        correct.add(AlgorithmTypes.BLAKE2B_512);
        correct.add(AlgorithmTypes.KECCAK_512);
        correct.add(AlgorithmTypes.BMW512);
        correct.add(AlgorithmTypes.ECHO512);
        correct.add(AlgorithmTypes.SHAvite512);
        correct.add(AlgorithmTypes.SIMD512);
        correct.add(AlgorithmTypes.Groestl512);
        correct.add(AlgorithmTypes.CubeHash512);

        assertEquals(correct, pow.getHashingAlgorithms());
    }
}
