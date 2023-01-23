package utils;

import io.coti.basenode.crypto.OriginatorCurrencyCrypto;
import io.coti.basenode.data.*;
import io.coti.basenode.http.GetHistoryAddressesRequest;
import io.coti.fullnode.http.FullNodeFeeRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TestUtils {

    private static final String[] hexaOptions = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
    private static final int SIZE_OF_HASH = 64;
    private static final String TRANSACTION_DESCRIPTION = "test";
    public static final String NATIVE_CURRENCY_HASH = "ae2b227ab7e614b8734be1f03d1532e66bf6caf76accc02ca4da6e28";

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

    public static TransactionData createRandomTransaction() {
        return createRandomTransaction(generateRandomHash(SIZE_OF_HASH));
    }

    private static TransactionData createRandomTransaction(Hash hash) {
        ArrayList<BaseTransactionData> baseTransactions = new ArrayList<>(
                Collections.singletonList(new InputBaseTransactionData
                        (generateRandomHash(SIZE_OF_HASH),
                                new Hash(NATIVE_CURRENCY_HASH),
                                new BigDecimal(0),
                                Instant.now())));
        return new TransactionData(baseTransactions,
                hash,
                TRANSACTION_DESCRIPTION,
                generateRandomTrustScore(),
                Instant.now(),
                TransactionType.Payment);
    }

    private static Double generateRandomTrustScore() {
        return Math.random() * 100;
    }

    public static GetHistoryAddressesRequest generateGetAddressesRequest() {
        List<Hash> hashes = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            hashes.add(generateRandomHash());
        }
        return new GetHistoryAddressesRequest(hashes);
    }

    public static FullNodeFeeRequest createFullNodeFeeRequest() {
        Hash currencyHash = OriginatorCurrencyCrypto.calculateHash("COTI");
        Hash userHash = generateRandomHash();
        FullNodeFeeRequest fullNodeFeeRequest = new FullNodeFeeRequest();
        fullNodeFeeRequest.setFeeIncluded(true);
        fullNodeFeeRequest.setOriginalAmount(new BigDecimal(1000));
        fullNodeFeeRequest.setUserHash(userHash);
        fullNodeFeeRequest.setOriginalCurrencyHash(currencyHash);
        return fullNodeFeeRequest;
    }

    public static FullNodeFeeData generateFullNodeFeeData(Hash hash, double amount) {
        return new FullNodeFeeData(hash,
                null,
                new BigDecimal(amount),
                null,
                new BigDecimal(1),
                Instant.now());
    }
}
