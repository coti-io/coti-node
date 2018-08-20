import io.coti.common.data.Hash;
import io.coti.common.data.TransactionData;
import io.coti.common.model.Transactions;
import io.coti.common.services.BalanceService;
import io.coti.common.services.ClusterService;
import io.coti.common.services.InitializationService;
import io.coti.common.services.LiveView.LiveViewService;
import io.coti.common.services.TccConfirmationService;
import io.coti.common.services.interfaces.ISourceSelector;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ClusterService.class)
@TestPropertySource(locations = "../fullnode1.properties")
@Slf4j

public class ClusterServiceTest {
    @Autowired
    private ClusterService cluster;

    @MockBean
    private Transactions transactions;

    @MockBean
    InitializationService initializationService;

    @MockBean
    private BalanceService balanceService;

    @MockBean
    private ISourceSelector sourceSelector;

    @MockBean
    private TccConfirmationService tccConfirmationService;

    @MockBean
    private LiveViewService liveViewService;

    private TransactionData transactionData0, transactionData1 ;

    @Before
    public void setUpTransactionsFromSnapShot() {

        transactionData0 = new TransactionData(new ArrayList<>(),new Hash("00"), "test", 70, new Date());
        // TransactionData0.setSenderTrustScore(70);
        List<Hash> hashChildren = new Vector<>();
        hashChildren.add(new Hash("11"));
        hashChildren.add(new Hash("00"));
        transactionData0.setChildrenTransactions(hashChildren);
        transactionData0.setProcessStartTime(new Date());
        transactionData0.setAttachmentTime(new Date());

        transactionData1 = new TransactionData(new ArrayList<>(),new Hash("11"), "test", 83, new Date());
        // TransactionData1.setSenderTrustScore(83);
        transactionData1.setRightParentHash(transactionData0.getHash());
        //  TransactionData1.setCreateTime(new Date());
        transactionData1.setProcessStartTime(new Date());
        transactionData1.setAttachmentTime(new Date());

        transactions.put(transactionData0);
        transactions.put(transactionData1);

        cluster.addUnconfirmedTransaction(transactionData0);
        cluster.addUnconfirmedTransaction(transactionData1);
    }

    @Test
    public void selectSources_noExceptionIsThrown(){
        try {
        TransactionData TransactionData = new TransactionData(new ArrayList<>(),new Hash("22"), "test", 92, new Date());
        cluster.selectSources(TransactionData);
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void attachToCluster_noExceptionIsThrown(){
        try {
            TransactionData TransactionData2 = new TransactionData(new ArrayList<>(),new Hash("22"), "test", 50, new Date());
            TransactionData2.setSenderTrustScore(92);
            TransactionData2.setLeftParentHash(new Hash("00"));
            when(transactions.getByHash(new Hash("00"))).thenReturn(transactionData0);
            cluster.attachToCluster(TransactionData2);
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void finalizeInit_noExceptionIsThrown() {
        try {
            cluster.finalizeInit();
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void addUnconfirmedTransaction_noExceptionIsThrown() {
        try {
            TransactionData TransactionData = new TransactionData(new ArrayList<>(),new Hash("22"), "test", 50, new Date());
            cluster.addUnconfirmedTransaction(TransactionData);
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @After
    public void tearDown() {
        cluster = null;
    }
}