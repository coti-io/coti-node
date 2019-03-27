package testUtils;

import io.coti.basenode.data.*;
import io.coti.basenode.http.GetBalancesRequest;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class BaseNodeTestUtils {

    private static final String[] hexaOptions = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
    private static final int SIZE_OF_HASH = 64;
    private static final int SIZE_OF_BASE_TRANSACTION_HASH = 136;
    private static final int MAX_TRUST_SCORE = 100;
    private static final int ANY_NUMBER = 10000;
    private static final String TRANSACTION_DESCRIPTION = "test";

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

    public static Double generateRandomTrustScore() {
        return Math.random() * MAX_TRUST_SCORE;
    }

    public static Double generateRandomPositiveAmount() {
        return Math.random() * ANY_NUMBER;
    }

    public static Double generateRandomNegativeAmount() {
        return -generateRandomPositiveAmount();
    }

    public static TransactionData createRandomTransaction(Hash hash) {
        ArrayList<BaseTransactionData> baseTransactions = new ArrayList<>(
                Collections.singletonList(new InputBaseTransactionData(
                        generateRandomHash(SIZE_OF_BASE_TRANSACTION_HASH),
                        new BigDecimal(0),
                        new Date())));
        return
                new TransactionData(baseTransactions,
                hash,
                TRANSACTION_DESCRIPTION,
                generateRandomTrustScore(),
                        Instant.ofEpochMilli(new Date().getTime()),
                TransactionType.Payment);
    }

    public static TransactionData createRandomTransaction() {
        return createRandomTransaction(generateRandomHash(SIZE_OF_HASH));
    }

    public static BaseTransactionData generateRandomInputBaseTransactionData(Hash hash, double count) {
        return new InputBaseTransactionData
                (hash,
                        new BigDecimal(count),
                        new Date());
    }

    public static OutputBaseTransactionData generateNetworkFeeData(Hash hash, double amount) {
        return generateNetworkFeeData(hash, amount, generateRandomPositiveAmount(), generateRandomPositiveAmount());
    }

    public static OutputBaseTransactionData generateNetworkFeeData(Hash hash, double count, double originalAmount, double reducedAmount) {
        return new NetworkFeeData(hash,
                new BigDecimal(count),
                new BigDecimal(originalAmount),
                new BigDecimal(reducedAmount),
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

    public static OutputBaseTransactionData generateRandomReceiverBaseTxData() {
        return generateReceiverBaseTransactionData(generateRandomHash(), generateRandomPositiveAmount());
    }

    public static BaseTransactionData generateRollingReserveData(Hash hash, double count) {
        return new RollingReserveData(hash,
                new BigDecimal(count),
                new BigDecimal(1),
                new BigDecimal(0.5),
                new Date());
    }

    public static AddressData generateRandomAddressData() {
        AddressData addressData = new AddressData(generateRandomHash());
        return addressData;
    }

    public static GetBalancesRequest generateRandomBalanceRequest() {
        GetBalancesRequest getBalancesRequest = new GetBalancesRequest();
        @NotNull(message = "Addresses must not be blank") List<Hash> addresses = Arrays.asList(generateRandomHash(),generateRandomHash());
        getBalancesRequest.setAddresses(addresses);
        return getBalancesRequest;
    }

    public static TransactionData generateRandomTxData() {
        List<BaseTransactionData> baseTxs = new ArrayList<>(
                Collections.singletonList(new InputBaseTransactionData(generateRandomHash(),
                        new BigDecimal(0),
                        new Date())));
        TransactionData txData = new TransactionData(baseTxs, generateRandomHash(), "Generated Tx ",
                generateRandomTrustScore(), Instant.ofEpochMilli(new Date().getTime()),
                TransactionType.Payment);
        return txData;
    }

    public static DspConsensusResult generateRandomDspConsensusResult() {
        Hash txHash = generateRandomHash();
        DspConsensusResult dspConsensusResult = new DspConsensusResult(txHash);
        dspConsensusResult.setIndex(0);
        dspConsensusResult.setDspConsensus(true);
        // TODO initialize additional fields as needed, consider wrapping with input hash if needed
        return dspConsensusResult;
    }
}


