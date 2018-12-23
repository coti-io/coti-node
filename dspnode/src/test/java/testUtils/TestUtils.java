package testUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.coti.basenode.data.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class TestUtils {

    public static Double generateRandomTrustScore() {
        return Math.random() * 100;
    }

    public static TransactionData createTransactionWithSpecificHash(Hash hash) {
        ArrayList<BaseTransactionData> baseTransactions = new ArrayList<BaseTransactionData>(
                Arrays.asList(new InputBaseTransactionData
                        (generateRandomHash(64),
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
    private static final String[] hexaOptions = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};

    public static Hash generateRandomHash(int lengthOfHash) {
        String hexa = "";
        for (int i = 0; i < lengthOfHash; i++) {
            int randomNum = ThreadLocalRandom.current().nextInt(0, 15 + 1);
            hexa += hexaOptions[randomNum];
        }
        return new Hash(hexa);
    }


    public static BaseTransactionData createBaseTransactionDataWithSpecificHash(Hash hash){
        return new InputBaseTransactionData
                (hash,
                        new BigDecimal(0),
                        new Date());
    }

}
