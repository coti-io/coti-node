package io.coti.trustscore.testutils;

import io.coti.basenode.data.Hash;

import java.util.concurrent.ThreadLocalRandom;

public class GeneralUtilsFunctions {
    private static final String[] hexaOptions = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

    public static boolean isTrustScoreValueValid(double score) {
        return score > 0 && score <= 100;
    }

    public static Hash generateRandomHash(int lengthOfHash) {
        String hexa = "";
        for (int i = 0; i < lengthOfHash; i++) {
            int randomNum = ThreadLocalRandom.current().nextInt(0, 15 + 1);
            hexa = hexa.concat(hexaOptions[randomNum]);
        }
        return new Hash(hexa);
    }

    public static Double generateRandomTrustScore() {
        return Math.random() * 100;
    }
}
