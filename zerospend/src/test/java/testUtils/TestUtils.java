package testUtils;

import io.coti.basenode.data.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
public class TestUtils {

    private static final String[] hexaOptions = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
    private static final int SIZE_OF_HASH = 64;
    private static final int ANY_NUMBER = 10000;
    public static final String ANY_ADDRESS = "localhost";
    public static final String ANY_HTTP_PORT = "8080";
    public static final String ANY_RECEIVING_PORT = "9090";

    private static double generateRandomTrustScore() {
        return Math.random() * 100;
    }

    public static Double generateRandomPositiveAmount() {
        return Math.random() * ANY_NUMBER;
    }

    public static double generateRandomCount() {
        return Math.random() * Double.MAX_VALUE;
    }

    public static long generateRandomLongNumber() {
        return (long) (Math.random() * Long.MAX_VALUE);
    }

    public static int generateRandomIntNumber() {
        return (int) (Math.random() * Integer.MAX_VALUE);
    }

    public static BigDecimal generateRandomPositiveBigDecimal() {
        return new BigDecimal( generateRandomLongNumber() );
        }

    public static TransactionData createRandomTransaction(Hash hash) {
        ArrayList<BaseTransactionData> baseTransactions = new ArrayList<>(
                Collections.singletonList(new InputBaseTransactionData(generateRandomHash(),
                        new BigDecimal(0),
                        new Date())));
        return new TransactionData(baseTransactions,
                hash,
                "test",
                generateRandomTrustScore(),
                Instant.ofEpochMilli(new Date().getTime()),
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

    public static TransactionData createRandomTransaction() {
        return createRandomTransaction(generateRandomHash(SIZE_OF_HASH));
    }

    public static BaseTransactionData createBaseTransactionDataWithSpecificHash(Hash hash) {
        return new InputBaseTransactionData
                (hash,
                        new BigDecimal(0),
                        new Date());
    }

    public static NetworkNodeData generateRandomNetworkNodeData() {
        NodeType nodeType = NodeType.DspNode;
        String address = ANY_ADDRESS;
        String httpPort = ANY_HTTP_PORT;
        Hash nodeHash = generateRandomHash();
        NetworkType networkType = NetworkType.TestNet;

        NetworkNodeData networkNodeData = new NetworkNodeData(nodeType, address, httpPort, nodeHash, networkType);
        networkNodeData.setReceivingPort(ANY_RECEIVING_PORT);
        return networkNodeData;
    }

    public static AddressData generateRandomAddressData() {
        AddressData addressData = new AddressData(generateRandomHash());
        return addressData;

    }

    public static NetworkData generateRandomNetworkData() {
        NetworkData networkData = new NetworkData();
        return networkData;
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

    public static DspVote generateRandomDspVote() {
        Hash voterDspHash = generateRandomHash();
        Hash txHash = generateRandomHash();
        DspVote dspVote = new DspVote(txHash, true);
        dspVote.setVoterDspHash(voterDspHash);
        return dspVote;
    }

    public static TransactionVoteData generateTxVoteDataByDspVote(DspVote dspVote) {
        // To enable modifications on list
        List voterDspHashList = new ArrayList();
        voterDspHashList.add(dspVote.getVoterDspHash());
        return new TransactionVoteData(dspVote.getHash(), voterDspHashList);
    }

    public static TransactionIndexData generateTransactionIndexData() {
        return new TransactionIndexData(generateRandomHash(), generateRandomLongNumber(), generateRandomHash().getBytes());
    }

    public static ZeroSpendTransactionRequest generateZeroSpendTxRequest() {
        TransactionData txData = createRandomTransaction();
        Hash zeroSpendTransactionRequestHash = generateRandomHash();
        ZeroSpendTransactionRequest zeroSpendTransactionRequest = new ZeroSpendTransactionRequest();
        zeroSpendTransactionRequest.setTransactionData(txData);
        zeroSpendTransactionRequest.setHash(zeroSpendTransactionRequestHash);
        return zeroSpendTransactionRequest;
    }
}
