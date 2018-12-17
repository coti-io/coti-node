package unitTest;

import io.coti.basenode.data.GraphData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.LiveView.LiveViewService;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;

import java.util.Date;

import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {LiveViewService.class})
public class LiveViewServiceTest {

    @Autowired
    private LiveViewService liveViewService;

    @MockBean
    private SimpMessagingTemplate messagingSender;

    @Test
    public void getFullGraph_noExceptionIsThrown() {
        try {
            liveViewService.getFullGraph();
        } catch (Exception e) {
            assertNull(e);
        }

    }

    @Test
    public void addNode_noExceptionIsThrown() {
        try {
            liveViewService.addNode(TestUtils.createTransactionWithSpecificHash(new Hash("ab")));
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void testSetNodeDataDatesFromTransactionData() {
        TransactionData transactionData =
                TestUtils.createTransactionWithSpecificHash(new Hash("bb"));
        transactionData.setAttachmentTime(new Date(200000));
        transactionData.setTransactionConsensusUpdateTime(new Date(300000));
        NodeData nodeData = new NodeData();
        liveViewService.setNodeDataDatesFromTransactionData(transactionData, nodeData);
        Assert.assertTrue(nodeData.getTccDuration() == 100);
    }

    @Test
    public void testUpdateNodeStatus() {
        TransactionData transactionData = TestUtils.createTransactionWithSpecificHash(new Hash("d1"));
        liveViewService.addNode(transactionData);
        liveViewService.updateNodeStatus(transactionData, 2);
        GraphData graphData = liveViewService.getFullGraph();
        Assert.assertTrue(graphData.nodes.get(0).getStatus() == 2);
    }
}
