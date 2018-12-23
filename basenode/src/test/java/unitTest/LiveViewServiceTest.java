package unitTest;

import io.coti.basenode.data.GraphData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NodeData;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.LiveView.LiveViewService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;

import java.util.Date;

import static testUtils.TestUtils.generateRandomHash;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {LiveViewService.class})
@Slf4j
public class LiveViewServiceTest {
    private static final int SIZE_OF_HASH = 64;

    private static final Hash TRANSACTION_ONE_HASH = generateRandomHash(SIZE_OF_HASH);
    private static final Hash TRANSACTION_TWO_HASH = generateRandomHash(SIZE_OF_HASH);
    private static final Hash TRANSACTION_THREE_HASH = generateRandomHash(SIZE_OF_HASH);

    private static final int HUNDRED_SECONDS = 100;
    private static final int HUNDRED_SECONDS_IN_MILLISECONDS = 100000;

    private static int TCC_CONFIRMED_STATUS = 2;

    @Autowired
    private LiveViewService liveViewService;

    @MockBean
    private SimpMessagingTemplate messagingSender;

    @Before
    public void init(){
        log.info("Starting  - " + this.getClass().getSimpleName());
    }

    @Test
    public void getFullGraph_noExceptionIsThrown() {
        try {
            liveViewService.getFullGraph();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

    }

    @Test
    public void addNode_noExceptionIsThrown() {
        try {
            liveViewService.addNode(TestUtils.createTransactionWithSpecificHash(TRANSACTION_ONE_HASH));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
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
