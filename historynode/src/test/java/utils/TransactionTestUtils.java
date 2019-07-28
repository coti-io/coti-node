package utils;

import io.coti.basenode.data.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public class TransactionTestUtils {

    public static TransactionData createRandomTransaction(Hash hash) {
        ArrayList<BaseTransactionData> baseTransactions = new ArrayList<>(
                Collections.singletonList(new InputBaseTransactionData(
                        HashTestUtils.generateRandomHash(TestConstants.SIZE_OF_BASE_TRANSACTION_HASH),
                        new BigDecimal(0),
                        Instant.now())));
        TransactionData transactionData = new TransactionData(baseTransactions,
                hash,
                TestConstants.TRANSACTION_DESCRIPTION,
                generateRandomTrustScore(),
                Instant.now(),
                TransactionType.Payment);
        return transactionData;
    }

    public static TransactionData createRandomTransaction() {
        return createRandomTransaction(HashTestUtils.generateRandomHash(TestConstants.SIZE_OF_HASH));
    }

    public static Double generateRandomTrustScore() {
        return Math.random() * TestConstants.MAX_TRUST_SCORE;
    }

    public static List<TransactionData> generateListOfRandomTransactionData(int listSize) {
        List<TransactionData> transactions = new ArrayList<>();
        for (int i = 0; i <= listSize; i++) {
            transactions.add(createRandomTransaction());
        }
        return transactions;
    }

    public static Set<TransactionData> generateSetOfRandomTransactionData(int listSize) {
        Set<TransactionData> transactions = new HashSet<>();
        for (int i = 0; i < listSize; i++) {
            transactions.add(createRandomTransaction());
        }
        return transactions;
    }

}
