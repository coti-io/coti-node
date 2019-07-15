package utils;

import io.coti.basenode.data.Hash;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class HashTestUtils {

    private static String[] hexaOptions = TestConstants.hexaOptions;
    private static int SIZE_OF_HASH = TestConstants.SIZE_OF_HASH;

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

    public static List<Hash> generateListOfRandomHashes(int listSize){
        List<Hash> hashes = new ArrayList<>();
        for (int i = 0 ; i <= listSize ; i++){
            hashes.add(generateRandomHash());
        }
        return hashes;
    }

    public static Set<Hash> generateSetOfRandomHashes(int listSize){
        Set<Hash> hashes = new HashSet<>();
        for (int i = 0 ; i <= listSize ; i++){
            hashes.add(generateRandomHash());
        }
        return hashes;
    }

}
