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

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = TccConfirmationService.class)
@TestPropertySource(locations = "../test.properties")
@Slf4j
public class TccConfirmationServiceTest {

    @Autowired
    TccConfirmationService tccConfirmationService;
    private ConcurrentHashMap<Hash, TransactionData> hashToUnConfirmationTransactionsMapping;
    private TransactionData transactionData0, transactionData1, transactionData2, transactionData3, transactionData4, TransactionData5, transactionData6;
    @Value("${cluster.trust.chain.threshold}")
    private int threshold;

    @Before
    public void init() {
        transactionData0 = new TransactionData(new ArrayList<>(), new Hash("00"), "test", 20, new Date(), TransactionType.Payment);
        transactionData1 = new TransactionData(new ArrayList<>(), new Hash("11"), "test", 70, new Date(), TransactionType.Payment);
        transactionData2 = new TransactionData(new ArrayList<>(), new Hash("22"), "test", 100, new Date(), TransactionType.Payment);
        transactionData3 = new TransactionData(new ArrayList<>(), new Hash("33"), "test", 90, new Date(), TransactionType.Payment);
        transactionData4 = new TransactionData(new ArrayList<>(), new Hash("44"), "test", 50, new Date(), TransactionType.Payment);
        TransactionData5 = new TransactionData(new ArrayList<>(), new Hash("55"), "test", 70, new Date(), TransactionType.Payment);
        transactionData6 = new TransactionData(new ArrayList<>(), new Hash("66"), "test", 60, new Date(), TransactionType.Payment);

        transactionData0.setLeftParentHash(transactionData1.getHash());
        transactionData0.setRightParentHash(transactionData2.getHash());
        transactionData1.setLeftParentHash(transactionData3.getHash());
        transactionData1.setRightParentHash(transactionData4.getHash());
        transactionData2.setRightParentHash(TransactionData5.getHash());
        transactionData6.setLeftParentHash(transactionData1.getHash());

        TransactionData5.setChildrenTransactions(new Vector<Hash>() {{
            add(transactionData2.getHash());
        }});
        transactionData2.setChildrenTransactions(new Vector<Hash>() {{
            add(transactionData0.getHash());
        }});
        transactionData1.setChildrenTransactions(new Vector<Hash>() {{
            add(transactionData6.getHash());
            add(transactionData0.getHash());
        }});
        transactionData3.setChildrenTransactions(new Vector<Hash>() {{
            add(transactionData1.getHash());
        }});
        transactionData4.setChildrenTransactions(new Vector<Hash>() {{
            add(transactionData1.getHash());
            // add(TransactionData5.getHash()); //?
        }});

        this.hashToUnConfirmationTransactionsMapping = new ConcurrentHashMap<Hash, TransactionData>() {{
            put(transactionData0.getHash(), transactionData0);
            put(transactionData1.getHash(), transactionData1);
            put(transactionData2.getHash(), transactionData2);
            put(transactionData3.getHash(), transactionData3);
            put(transactionData4.getHash(), transactionData4);
            put(TransactionData5.getHash(), TransactionData5);
            put(transactionData6.getHash(), transactionData6);
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
        transactionData4.setSenderTrustScore(85);
        List<TccInfo> allTransactions = tccConfirmationService.getTccConfirmedTransactions();
        // Because it's a test, the TccConfirmationService class sets threshold=0, so we will filter it here
        List<TccInfo> transactionConsensusConfirmed = allTransactions.stream()
                .filter(t -> t.getTrustChainTrustScore() > threshold)
                .collect(Collectors.toList());
        Assert.assertTrue(transactionConsensusConfirmed.size() == 2);
    }
}