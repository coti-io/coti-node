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
import static testUtils.TestUtils.generateRandomHash;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {LiveViewService.class})
public class LiveViewServiceTest {
    public static final int SIZE_OF_HASH = 64;

    public static final Hash TRANSACTION_ONE_HASH = generateRandomHash(SIZE_OF_HASH);
    public static final Hash TRANSACTION_TWO_HASH = generateRandomHash(SIZE_OF_HASH);
    public static final Hash TRANSACTION_THREE_HASH = generateRandomHash(SIZE_OF_HASH);

    public static final int HUNDRED_SECONDS = 100;
    public static final int HUNDRED_SECONDS_IN_MILLISECONDS = 100000;

    public static int TCC_CONFIRMED_STATUS = 2;

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
            liveViewService.addNode(TestUtils.createTransactionWithSpecificHash(TRANSACTION_ONE_HASH));
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void testSetNodeDataDatesFromTransactionData() {
        TransactionData transactionData =
                TestUtils.createTransactionWithSpecificHash(TRANSACTION_TWO_HASH);
        transactionData.setAttachmentTime(new Date());
        transactionData.setTransactionConsensusUpdateTime(new Date(transactionData.getAttachmentTime().getTime() + HUNDRED_SECONDS_IN_MILLISECONDS));
        NodeData nodeData = new NodeData();
        liveViewService.setNodeDataDatesFromTransactionData(transactionData, nodeData);
        Assert.assertTrue(nodeData.getTccDuration() == HUNDRED_SECONDS);
    }

    @Test
    public void testUpdateNodeStatus() {
        TransactionData transactionData = TestUtils.createTransactionWithSpecificHash(TRANSACTION_THREE_HASH);
        liveViewService.addNode(transactionData);
        liveViewService.updateNodeStatus(transactionData, TCC_CONFIRMED_STATUS);
        GraphData graphData = liveViewService.getFullGraph();
        Assert.assertTrue(graphData.nodes.get(0).getStatus() == TCC_CONFIRMED_STATUS);
    }
}
