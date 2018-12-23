package unitTest;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TccInfo;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.TransactionType;
import io.coti.basenode.services.TccConfirmationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static testUtils.TestUtils.generateRandomHash;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TccConfirmationService.class)
@TestPropertySource(locations = "../test.properties")
@Slf4j
public class TccConfirmationServiceTest {
    private static final String TRANSACTION_DESCRIPTION = "test";
    private static final int SIZE_OF_HASH = 64;
    @Autowired
    TccConfirmationService tccConfirmationService;
    private List<TransactionData> newTransactions;
    private ConcurrentHashMap<Hash, TransactionData> hashToUnConfirmationTransactionsMapping;

    @Value("${cluster.trust.chain.threshold}")
    private int threshold;

    @Before
    public void init() {
        log.info("Starting  - " + this.getClass().getSimpleName());
        newTransactions = new Vector();
        double[] trustScores = new double[]{20, 70, 100, 90, 50, 70, 60};
        for (int i = 0; i < 7; i++) {
            TransactionData transactionData = new TransactionData(new ArrayList<>(), generateRandomHash(SIZE_OF_HASH), TRANSACTION_DESCRIPTION, trustScores[i], new Date(), TransactionType.Payment);
            newTransactions.add(transactionData);
        }

        newTransactions.get(0).setLeftParentHash(newTransactions.get(1).getHash());
        newTransactions.get(0).setRightParentHash(newTransactions.get(2).getHash());
        newTransactions.get(1).setLeftParentHash(newTransactions.get(3).getHash());
        newTransactions.get(1).setRightParentHash(newTransactions.get(4).getHash());
        newTransactions.get(2).setRightParentHash(newTransactions.get(5).getHash());
        newTransactions.get(6).setLeftParentHash(newTransactions.get(1).getHash());

        newTransactions.get(5).setChildrenTransactions(new Vector<Hash>() {{
            add(newTransactions.get(2).getHash());
        }});
        newTransactions.get(2).setChildrenTransactions(new Vector<Hash>() {{
            add(newTransactions.get(0).getHash());
        }});
        newTransactions.get(1).setChildrenTransactions(new Vector<Hash>() {{
            add(newTransactions.get(6).getHash());
            add(newTransactions.get(0).getHash());
        }});
        newTransactions.get(3).setChildrenTransactions(new Vector<Hash>() {{
            add(newTransactions.get(1).getHash());
        }});
        newTransactions.get(4).setChildrenTransactions(new Vector<Hash>() {{
            add(newTransactions.get(1).getHash());
        }});

        this.hashToUnConfirmationTransactionsMapping = new ConcurrentHashMap<Hash, TransactionData>() {{
            put(newTransactions.get(0).getHash(), newTransactions.get(0));
            put(newTransactions.get(1).getHash(), newTransactions.get(1));
            put(newTransactions.get(2).getHash(), newTransactions.get(2));
            put(newTransactions.get(3).getHash(), newTransactions.get(3));
            put(newTransactions.get(4).getHash(), newTransactions.get(4));
            put(newTransactions.get(5).getHash(), newTransactions.get(5));
            put(newTransactions.get(6).getHash(), newTransactions.get(6));
        }};

        tccConfirmationService = new TccConfirmationService();
        tccConfirmationService.init(hashToUnConfirmationTransactionsMapping);
    }


    @Test
    public void getTccConfirmedTransactions_whenOnlyOneTransactionPassesTheThreshold() {
        List<TccInfo> allTransactions = tccConfirmationService.getTccConfirmedTransactions();
        // Because it's a test, the TccConfirmationService class sets threshold=0, so we will filter it here
        List<TccInfo> transactionConsensusConfirmed = allTransactions.stream()
                .filter(t -> t.getTrustChainTrustScore() > threshold)
                .collect(Collectors.toList());
        Assert.assertTrue(transactionConsensusConfirmed.size() == 1);
    }

    @Test
    public void getTccConfirmedTransactions_whenTwoTransactionPassesTheThreshold() {
        newTransactions.get(4).setSenderTrustScore(85);
        List<TccInfo> allTransactions = tccConfirmationService.getTccConfirmedTransactions();
        // Because it's a test, the TccConfirmationService class sets threshold=0, so we will filter it here
        List<TccInfo> transactionConsensusConfirmed = allTransactions.stream()
                .filter(t -> t.getTrustChainTrustScore() > threshold)
                .collect(Collectors.toList());
        Assert.assertTrue(transactionConsensusConfirmed.size() == 2);
    }
}