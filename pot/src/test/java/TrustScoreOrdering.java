import io.coti.pot.ProofOfTrust;
import io.coti.pot.interfaces.IAlgorithm;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TrustScoreOrdering {

    @Test
    public void trustScoreOrdering() {
        List<IAlgorithm.AlgorithmTypes> masterList = Arrays.asList(IAlgorithm.AlgorithmTypes.values());


        for (int i = 0; i <= 100; i++) {


            ProofOfTrust pow1 = new ProofOfTrust(i);
            ProofOfTrust pow2 = new ProofOfTrust(i);
            ProofOfTrust pow3 = new ProofOfTrust(i);
            ProofOfTrust pow4 = new ProofOfTrust(i);

            int count1 = pow1.getHashingAlgorithms().size();
            int count2 = pow2.getHashingAlgorithms().size();
            int count3 = pow3.getHashingAlgorithms().size();
            int count4 = pow4.getHashingAlgorithms().size();

            int hope = -1;
            if (i <= 6) {
                hope = 16;
            } else if (i <= 12) {
                hope = 15;
            } else if (i <= 18) {
                hope = 14;
            } else if (i <= 25) {
                hope = 13;
            } else if (i <= 31) {
                hope = 12;
            } else if (i <= 37) {
                hope = 11;
            } else if (i <= 43) {
                hope = 10;
            } else if (i <= 50) {
                hope = 9;
            } else if (i <= 56) {
                hope = 8;
            } else if (i <= 62) {
                hope = 7;
            } else if (i <= 68) {
                hope = 6;
            } else if (i <= 75) {
                hope = 5;
            } else if (i <= 81) {
                hope = 4;
            } else if (i <= 87) {
                hope = 3;
            } else if (i <= 93) {
                hope = 2;
            } else if (i <= 100) {
                hope = 1;
            }

            assertEquals(hope, count1);
            assertEquals(hope, count2);
            assertEquals(hope, count3);
            assertEquals(hope, count4);

            List<IAlgorithm.AlgorithmTypes> order1 = pow1.getHashingAlgorithms();
            List<IAlgorithm.AlgorithmTypes> order2 = pow2.getHashingAlgorithms();
            List<IAlgorithm.AlgorithmTypes> order3 = pow3.getHashingAlgorithms();
            List<IAlgorithm.AlgorithmTypes> order4 = pow4.getHashingAlgorithms();

            assertEquals(order1, order3);
            assertEquals(order2, order4);

            if (count1 > 2) {
                assertNotEquals(order1, order2);
                assertNotEquals(order3, order4);
            }

            correctSubset(masterList, order1, count1);
            correctSubset(masterList, order2, count2);
            correctSubset(masterList, order3, count3);
            correctSubset(masterList, order4, count4);

        }
    }

    private void correctSubset(List<IAlgorithm.AlgorithmTypes> masterList, List<IAlgorithm.AlgorithmTypes> order, int count) {
        List<IAlgorithm.AlgorithmTypes> shortList = masterList.subList(0, count);
        for (IAlgorithm.AlgorithmTypes algo : order) {
            assertTrue(shortList.contains(algo));
        }
    }
}
