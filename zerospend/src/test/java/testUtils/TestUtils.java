package testUtils;

import io.coti.basenode.data.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class TestUtils {

    private static final String[] hexaOptions = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
    private static final int SIZE_OF_HASH = 64;

    private static Double generateRandomTrustScore() {
        return Math.random() * 100;
    }

    public static double generateRandomCount() {
        return Math.random() * Double.MAX_VALUE;
    }

    public static long generateRandomLongNumber() {
        return (long) (Math.random() * Long.MAX_VALUE);
    }

    public static TransactionData generateRandomTransaction() {
        return generateRandomTransaction(generateRandomHash(SIZE_OF_HASH));
    }

    private static TransactionData generateRandomTransaction(Hash hash) {
        ArrayList<BaseTransactionData> baseTransactions = new ArrayList<>(
                Collections.singletonList(new InputBaseTransactionData
                        (generateRandomHash(SIZE_OF_HASH),
                                new BigDecimal(0),
                                new Date())));
        return new TransactionData(baseTransactions,
                hash,
                "test",
                generateRandomTrustScore(),
                new Date(),
                TransactionType.Payment);
    }

    public static Hash generateRandomHash() {
        return generateRandomHash(SIZE_OF_HASH);
    }

    private static Hash generateRandomHash(int lengthOfHash) {
        StringBuilder hexa = new StringBuilder();
        for (int i = 0; i < lengthOfHash; i++) {
            int randomNum = ThreadLocalRandom.current().nextInt(0, 15 + 1);
            hexa.append(hexaOptions[randomNum]);
        }
        return new Hash(hexa.toString());
    }


    public static BaseTransactionData createBaseTransactionDataWithSpecificHash(Hash hash) {
        return new InputBaseTransactionData
                (hash,
                        new BigDecimal(0),
                        new Date());
    }

}
