package testUtils;

import io.coti.basenode.data.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

public class TestUtils {

    private static final String[] hexaOptions = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
    private static final int SIZE_OF_HASH = 64;
    private static final int SIZE_OF_BASE_TRANSACTION_HASH = 136;
    private static final int MAX_TRUST_SCORE = 100;
    private static final int ANY_NUMBER = 10000;
    private static final String TRANSACTION_DESCRIPTION = "test";

    public static Hash generateRandomHash(int lengthOfHash) {
        StringBuilder hexa = new StringBuilder();
        for (int i = 0; i < lengthOfHash; i++) {
            int randomNum = ThreadLocalRandom.current().nextInt(0, 15 + 1);
            hexa.append(hexaOptions[randomNum]);
        }
        return new Hash(hexa.toString());
    }

    public static Hash generateRandomHash() {
        return generateRandomHash(SIZE_OF_HASH);
    }

    public static TransactionData createRandomTransaction(Hash hash) {
        ArrayList<BaseTransactionData> baseTransactions = new ArrayList<>(
                Collections.singletonList(new InputBaseTransactionData(
                        generateRandomHash(SIZE_OF_BASE_TRANSACTION_HASH),
                        new BigDecimal(0),
                        new Date())));
        return new TransactionData(baseTransactions,
                hash,
                TRANSACTION_DESCRIPTION,
                generateRandomTrustScore(),
                new Date(),
                TransactionType.Payment);
    }

    public static TransactionData createRandomTransaction() {
        return createRandomTransaction(generateRandomHash(SIZE_OF_HASH));
    }

    public static Double generateRandomTrustScore() {
        return Math.random() * MAX_TRUST_SCORE;
    }

}
