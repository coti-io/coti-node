package utils;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.*;
import io.coti.basenode.http.GetHistoryAddressesRequest;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TestUtils {

    private static final String[] hexaOptions = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
    private static final int SIZE_OF_HASH = 64;
    private static final int SIZE_OF_ADDRESS_HASH = 136;
    private static final String TRANSACTION_DESCRIPTION = "test";

    public static Hash generateRandomHash() {
        return generateRandomHash(SIZE_OF_HASH);
    }

    public static Hash generateRandomAddressHash() {
        return generateRandomHash(SIZE_OF_ADDRESS_HASH);
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

    public static TransactionData createRandomTransactionWithSenderAddress() {
        TransactionData transactionData = createRandomTransaction(generateRandomHash(SIZE_OF_HASH));
        transactionData.setSenderHash(generateRandomHash());
        return transactionData;
    }

    public static TransactionData createRandomTransactionWithSenderAddress(Hash hash) {
        TransactionData transactionData = createRandomTransaction(generateRandomHash(SIZE_OF_HASH));
        transactionData.setSenderHash(hash);
        return transactionData;
    }

    private static TransactionData createRandomTransaction(Hash hash) {
        ArrayList<BaseTransactionData> baseTransactions = new ArrayList<>(
                Collections.singletonList(new InputBaseTransactionData
                        (HashTestUtils.generateRandomAddressHash(),
                                new BigDecimal(0),
                                Instant.now())));
        byte[] bytesToHash = getBaseMessageInBytes(baseTransactions.get(0));
        baseTransactions.get(0).setHash(CryptoHelper.cryptoHash(bytesToHash));
        SignatureData signatureData = new SignatureData();
        signatureData.setS("a");
        signatureData.setR("b");
        baseTransactions.get(0).setSignature(signatureData);
        return new TransactionData(baseTransactions,
                hash,
                TRANSACTION_DESCRIPTION,
                generateRandomTrustScore(),
                Instant.now(),
                TransactionType.Transfer);
    }

    protected static byte[] getBaseMessageInBytes(BaseTransactionData baseTransactionData) {
        byte[] addressBytes = baseTransactionData.getAddressHash().getBytes();
        String decimalStringRepresentation = baseTransactionData.getAmount().stripTrailingZeros().toPlainString();
        byte[] bytesOfAmount = decimalStringRepresentation.getBytes(StandardCharsets.UTF_8);

        Instant createTime = baseTransactionData.getCreateTime();
        byte[] createTimeInBytes = ByteBuffer.allocate(Long.BYTES).putLong(createTime.toEpochMilli()).array();

        ByteBuffer baseTransactionBuffer = ByteBuffer.allocate(addressBytes.length + bytesOfAmount.length + createTimeInBytes.length).
                put(addressBytes).put(bytesOfAmount).put(createTimeInBytes);

        return baseTransactionBuffer.array();
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
}
