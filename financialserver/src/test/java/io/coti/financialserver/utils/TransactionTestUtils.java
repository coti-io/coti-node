package io.coti.financialserver.utils;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.*;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static io.coti.financialserver.utils.TestConstants.*;

public class TransactionTestUtils {


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

    public static TransactionData createRandomTransactionWithAddress(Hash addressHash) {
        Hash hash = generateRandomHash(SIZE_OF_HASH);
        ArrayList<BaseTransactionData> baseTransactions = new ArrayList<>();
        baseTransactions.add(new InputBaseTransactionData(addressHash, new Hash(NATIVE_CURRENCY_HASH),
                new BigDecimal(-5), Instant.now()));
        baseTransactions.add(new ReceiverBaseTransactionData(addressHash, new Hash(NATIVE_CURRENCY_HASH),
                new BigDecimal(5), new Hash(NATIVE_CURRENCY_HASH), new BigDecimal(5), Instant.now()));
        byte[] bytesToHash = getBaseMessageInBytes(baseTransactions.get(0));
        baseTransactions.get(0).setHash(CryptoHelper.cryptoHash(bytesToHash));
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

}
