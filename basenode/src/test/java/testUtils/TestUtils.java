package testUtils;

import io.coti.basenode.data.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class TestUtils {

    private static final String[] hexaOptions = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

    public static Hash generateRandomHash(int lengthOfHash) {
        String hexa = "";
        for (int i = 0; i < lengthOfHash; i++) {
            int randomNum = ThreadLocalRandom.current().nextInt(0, 15 + 1);
            hexa += hexaOptions[randomNum];
        }
        return new Hash(hexa);
    }

    public static Double generateRandomTrustScore() {
        return Math.random() * 100;
    }

    public static Double generateRandomCount() {
        return Math.random() * Double.MAX_VALUE;
    }

    public static TransactionData createTransactionWithSpecificHash(Hash hash) {
        ArrayList<BaseTransactionData> baseTransactions = new ArrayList<BaseTransactionData>(
                Arrays.asList(new InputBaseTransactionData(
                        generateRandomHash(136),
                        new BigDecimal(0),
                        new Date())));
        TransactionData tx = new TransactionData(baseTransactions,
                hash,
                "test",
                generateRandomTrustScore(),
                new Date(),
                TransactionType.Payment);
        return tx;
    }

    public static TransactionData generateRandomTransaction() {
        return createTransactionWithSpecificHash(generateRandomHash(64));
    }


    public static BaseTransactionData createInputBaseTransactionDataWithSpecificHashAndCount(Hash hash, double count) {
        return new InputBaseTransactionData
                (hash,
                        new BigDecimal(count),
                        new Date());
    }

    public static OutputBaseTransactionData generateNetworkFeeData(Hash hash, double count) {
        return new NetworkFeeData(hash,
                new BigDecimal(count),
                new BigDecimal(1),
                new BigDecimal(3),
                new Date());
    }

    public static BaseTransactionData generateFullNodeFeeData(Hash hash, double count) {
        return new FullNodeFeeData(hash,
                new BigDecimal(count),
                new BigDecimal(1),
                new Date());
    }

    public static OutputBaseTransactionData generateReceiverBaseTransactionData(Hash hash, double count) {
        return new ReceiverBaseTransactionData(hash,
                new BigDecimal(count),
                new BigDecimal(1),
                new Date());
    }

    public static BaseTransactionData generateRollingReserveData(Hash hash, double count) {
        return new RollingReserveData(hash,
                new BigDecimal(count),
                new BigDecimal(1),
                new BigDecimal(0.5),
                new Date());
    }

    public static boolean setCurrentDirectory(String directory_name) {
        boolean result = false;  // Boolean indicating whether directory was set
        File directory;       // Desired current working directory

        directory = new File(directory_name).getAbsoluteFile();
        if (directory.exists() || directory.mkdirs()) {
            result = (System.setProperty(directory_name, directory.getAbsolutePath()) != null);
        }

        return result;
    }
}


