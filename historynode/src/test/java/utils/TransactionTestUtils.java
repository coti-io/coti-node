package utils;

import io.coti.basenode.data.Hash;

import java.util.concurrent.ThreadLocalRandom;

import static utils.TestConstants.SIZE_OF_HASH;
import static utils.TestConstants.hexaOptions;

public class TransactionTestUtils {

    public static Hash generateRandomHash() {
        return generateRandomHash(SIZE_OF_HASH);
    }

    public static Hash generateRandomHash(int lengthOfHash) {
        StringBuilder hexa = new StringBuilder();
        for (int i = 0; i < lengthOfHash; i++) {
            int randomNum = ThreadLocalRandom.current().nextInt(0, 15 + 1);
            hexa.append(hexaOptions[randomNum]);
        }
        return new Hash(hexa.toString());
    }
}
