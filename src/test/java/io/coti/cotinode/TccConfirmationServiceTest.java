package io.coti.cotinode;

import io.coti.cotinode.data.Hash;
import io.coti.cotinode.data.TransactionData;
import io.coti.cotinode.service.TccConfirmationService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

@SpringBootTest
public class TccConfirmationServiceTest {

    ConcurrentHashMap<Hash, TransactionData> hashToUnConfirmationTransactionsMapping;

    TccConfirmationService tccConfirmationService;

    @Before
    public void init() {

        TransactionData TransactionData0 = new TransactionData(new Hash("0".getBytes()));
        TransactionData0.setSenderTrustScore(80);

        TransactionData TransactionData1 = new TransactionData(new Hash("1".getBytes()));
        TransactionData1.setSenderTrustScore(120);

        TransactionData TransactionData2 = new TransactionData(new Hash("2".getBytes()));
        TransactionData2.setSenderTrustScore(100);

        TransactionData TransactionData3 = new TransactionData(new Hash("3".getBytes()));
        TransactionData3.setSenderTrustScore(90);

        TransactionData TransactionData4 = new TransactionData(new Hash("4".getBytes()));
        TransactionData4.setSenderTrustScore(100);

        TransactionData TransactionData5 = new TransactionData(new Hash("5".getBytes()));
        TransactionData5.setSenderTrustScore(90);


        TransactionData0.setLeftParent(TransactionData1.getHash());
        TransactionData0.setRightParent(TransactionData2.getHash());
        TransactionData1.setLeftParent(TransactionData3.getHash());
        TransactionData1.setRightParent(TransactionData4.getHash());
        TransactionData2.setRightParent(TransactionData5.getHash());
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
        tccConfirmationService.topologicalSorting();
        List<Hash> transactionConsensusConfirmed = tccConfirmationService.setTransactionConsensus();
        Assert.assertTrue(transactionConsensusConfirmed.size() == 1);
    }
}