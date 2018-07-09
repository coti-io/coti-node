import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.services.TccConfirmationService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootTest
public class TccConfirmationServiceTest {

    ConcurrentHashMap<Hash, TransactionData> hashToUnConfirmationTransactionsMapping;

    TccConfirmationService tccConfirmationService;

    @Before
    public void init() {
        TransactionData TransactionData0 = new TransactionData(new ArrayList<>(),new Hash("0"),"test",20);
        TransactionData0.setSenderTrustScore(80);

        TransactionData TransactionData1 = new TransactionData(new ArrayList<>(),new Hash("1".getBytes()),"test",20);
        TransactionData1.setSenderTrustScore(120);

        TransactionData TransactionData2 = new TransactionData(new ArrayList<>(),new Hash("2".getBytes()),"test",20);
        TransactionData2.setSenderTrustScore(100);

        TransactionData TransactionData3 = new TransactionData(new ArrayList<>(),new Hash("3".getBytes()),"test",20);
        TransactionData3.setSenderTrustScore(90);

        TransactionData TransactionData4 = new TransactionData(new ArrayList<>(),new Hash("4".getBytes()),"test",20);
        TransactionData4.setSenderTrustScore(100);

        TransactionData TransactionData5 = new TransactionData(new ArrayList<>(),new Hash("5".getBytes()),"test",20);
        TransactionData5.setSenderTrustScore(90);


//        TransactionData0.setLeftParentHash(TransactionData1);
//        TransactionData0.setRightParentHash(TransactionData2);
//        TransactionData1.setLeftParentHash(TransactionData3);
//        TransactionData1.setRightParentHash(TransactionData4);
//        TransactionData2.setRightParentHash(TransactionData5);
        // TransactionData5.setLeftParent(TransactionData4.getHash()); //?

        TransactionData5.setChildrenTransactions(new Vector<Hash>() {{
            add(TransactionData2.getHash());
        }});
        TransactionData2.setChildrenTransactions(new Vector<Hash>() {{
            add(TransactionData0.getHash());
        }});
        TransactionData1.setChildrenTransactions(new Vector<Hash>() {{
            add(TransactionData0.getHash());
        }});
        TransactionData1.setChildrenTransactions(new Vector<Hash>() {{
            add(TransactionData0.getHash());
        }});
        TransactionData3.setChildrenTransactions(new Vector<Hash>() {{
            add(TransactionData1.getHash());
        }});
        TransactionData4.setChildrenTransactions(new Vector<Hash>() {{
            add(TransactionData1.getHash());
            // add(TransactionData5.getHash()); //?
        }});

        this.hashToUnConfirmationTransactionsMapping = new ConcurrentHashMap<Hash, TransactionData>() {{
            put(TransactionData0.getHash(), TransactionData0);
            put(TransactionData1.getHash(), TransactionData1);
            put(TransactionData2.getHash(), TransactionData2);
            put(TransactionData3.getHash(), TransactionData3);
            put(TransactionData4.getHash(), TransactionData4);
            put(TransactionData5.getHash(), TransactionData5);
        }};

        tccConfirmationService = new TccConfirmationService();
        tccConfirmationService.init(hashToUnConfirmationTransactionsMapping);
    }


    @Test
    public void process() {
    }

    @Test
    public void findTransactionToconfirm() {
    }

    @Test
    public void topologicSorting() {

    }

    @Test
    public void setTransactionConsensus() {
//        tccConfirmationService.sortByTopologicalOrder();
//        List<Hash> transactionConsensusConfirmed = tccConfirmationService.getTccConfirmedTransactions();
//        Assert.assertTrue(transactionConsensusConfirmed.size() == 1);
    }
}