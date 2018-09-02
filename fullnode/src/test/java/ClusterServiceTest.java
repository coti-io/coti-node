import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.ClusterService;
import io.coti.fullnode.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AppConfig.class)
@Slf4j

public class ClusterServiceTest {

    @Autowired
    private Transactions transaction;
    @Autowired
    private ClusterService cluster;
    private List<Hash> notTccConfirmTransactions;

    @Test
    public void selectSources() {
        TransactionData TransactionData2 = new TransactionData(new ArrayList<>(), new Hash("22"), "test", 50, new Date());
        TransactionData2.setSenderTrustScore(92);
        //  TransactionData2.setCreateTime(new Date());
        cluster.selectSources(TransactionData2);
        Assert.assertEquals(TransactionData2.getLeftParentHash(), new Hash("11"));
    }

    @Test
    public void selectSourcesWithToHighTrustScore() {
        TransactionData TransactionData2 = new TransactionData(new ArrayList<>(), new Hash("22"), "test", 50, new Date());
        TransactionData2.setSenderTrustScore(99);
        //  TransactionData2.setCreateTime(new Date());
        cluster.selectSources(TransactionData2);
        Assert.assertEquals(TransactionData2.getLeftParentHash(), null);
    }

    @Test
    public void selectSourcesWithToLowTrustScore() {
        TransactionData TransactionData2 = new TransactionData(new ArrayList<>(), new Hash("22"), "test", 50, new Date());
        TransactionData2.setSenderTrustScore(50);
        //   TransactionData2.setCreateTime(new Date());
        cluster.selectSources(TransactionData2);
        Assert.assertEquals(TransactionData2.getLeftParentHash(), null);
    }

    @Test
    public void attachToCluster() {
        Exception ex = null;
        try {
            TransactionData TransactionData2 = new TransactionData(new ArrayList<>(), new Hash("22"), "test", 50, new Date());
            TransactionData2.setSenderTrustScore(92);
            //   TransactionData2.setCreateTime(new Date());
            TransactionData2.setLeftParentHash(new Hash("00"));
            cluster.attachToCluster(TransactionData2);
        } catch (Exception e) {
            ex = null;
        }
        Assert.assertEquals(null, ex);
    }

    private void setUpTransactionsFromSnapShot() {
        TransactionData TransactionData0 = new TransactionData(new ArrayList<>(), new Hash("00"), "test", 50, new Date());
        TransactionData0.setSenderTrustScore(70);
        List<Hash> hashChildren = new Vector<>();
        hashChildren.add(new Hash("11"));
        TransactionData0.setChildrenTransactions(hashChildren);
        TransactionData0.setProcessStartTime(new Date());
        TransactionData0.setAttachmentTime(new Date());

        TransactionData TransactionData1 = new TransactionData(new ArrayList<>(), new Hash("11"), "test", 50, new Date());
        TransactionData1.setSenderTrustScore(83);
        TransactionData1.setRightParentHash(TransactionData0.getHash());
        //  TransactionData1.setCreateTime(new Date());
        TransactionData1.setProcessStartTime(new Date());
        TransactionData1.setAttachmentTime(new Date());

        transaction.put(TransactionData0);
        transaction.put(TransactionData1);

        notTccConfirmTransactions.add(TransactionData0.getHash());
        notTccConfirmTransactions.add(TransactionData1.getHash());
    }

    @After
    public void tearDown() {
    }


}