package unitTest;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeBalanceService;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.ClusterService;
import io.coti.basenode.services.LiveView.LiveViewService;
import io.coti.basenode.services.TccConfirmationService;
import io.coti.basenode.services.interfaces.ISourceSelector;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestPropertySource(locations = "../test.properties")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ClusterService.class)
@Slf4j
public class ClusterServiceTest {
    @MockBean
    BaseNodeInitializationService initializationService;
    @Autowired
    private ClusterService cluster;
    @MockBean
    private Transactions transactions;
    @MockBean
    private BaseNodeBalanceService balanceService;
    @MockBean
    private ISourceSelector sourceSelector;
    @MockBean
    private TccConfirmationService tccConfirmationService;
    @MockBean
    private LiveViewService liveViewService;
    private TransactionData transactionData0, transactionData1;

    @Before
    public void setUpTransactions() {

        transactionData0 = new TransactionData(new ArrayList<>(), new Hash("00"), "test", 70, new Date());
        List<Hash> hashChildren = new Vector<>();
        transactionData0.setChildrenTransactions(hashChildren);
        transactionData0.setProcessStartTime(new Date());
        transactionData0.setAttachmentTime(new Date());

        transactionData1 = new TransactionData(new ArrayList<>(), new Hash("11"), "test", 83, new Date());
        transactionData1.setRightParentHash(transactionData0.getHash());
        transactionData1.setProcessStartTime(new Date());
        transactionData1.setAttachmentTime(new Date());

        transactions.put(transactionData0);
        transactions.put(transactionData1);

        cluster.addUnconfirmedTransaction(transactionData0);
        cluster.addUnconfirmedTransaction(transactionData1);
    }

    @Test
    public void attachToCluster_noExceptionIsThrown() {
        try {
            TransactionData TransactionData2 = new TransactionData(new ArrayList<>(), new Hash("22"), "test", 50, new Date());
            TransactionData2.setSenderTrustScore(92);
            TransactionData2.setLeftParentHash(new Hash("00"));
            when(transactions.getByHash(new Hash("00"))).thenReturn(transactionData0);
            cluster.attachToCluster(TransactionData2);
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void selectSources_onlyTransactionData1SourceAvailable_transactionData1AsLeftParent() {
        TransactionData TransactionData = new TransactionData(new ArrayList<>(), new Hash("22"), "test", 92, new Date());
        when(sourceSelector.selectSourcesForAttachment(any(List.class),
                any(double.class)))
                .thenReturn(new Vector<>(Arrays.asList(transactionData1)));
        cluster.selectSources(TransactionData);
        Assert.assertEquals(TransactionData.getLeftParentHash(), new Hash("11"));
    }

    @Test
    public void selectSources_twoSourcesAvailable_twoSourcesAsParents() {
        TransactionData TransactionData = new TransactionData(new ArrayList<>(), new Hash("22"), "test", 92, new Date());
        when(sourceSelector.selectSourcesForAttachment(any(List.class),
                any(double.class)))
                .thenReturn(new Vector<>(Arrays.asList(transactionData0, transactionData1)));
        cluster.selectSources(TransactionData);
        Assert.assertTrue(TransactionData.getLeftParentHash().equals(new Hash("00")) &&
                TransactionData.getRightParentHash().equals(new Hash("11")));
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
            TransactionData TransactionData = new TransactionData(new ArrayList<>(), new Hash("22"), "test", 50, new Date());
            cluster.addUnconfirmedTransaction(TransactionData);
        } catch (Exception e) {
            assertNull(e);
        }
    }
}