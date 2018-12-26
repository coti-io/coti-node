package io.coti.basenode.services;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import testUtils.TestUtils;

import java.util.Date;

import static testUtils.TestUtils.generateRandomHash;

@ContextConfiguration(classes = {LiveViewService.class})
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
@Slf4j
public class LiveViewServiceTest {

    private static final Hash TRANSACTION_ONE_HASH = generateRandomHash();
    private static final Hash TRANSACTION_TWO_HASH = generateRandomHash();

    private static final int HUNDRED_SECONDS = 100;
    private static final int HUNDRED_SECONDS_IN_MILLISECONDS = 100000;

    private static final int TCC_CONFIRMED_STATUS = 2;

    @Autowired
    private LiveViewService liveViewService;

    @MockBean
    private SimpMessagingTemplate messagingSender;

    @Before
    public void setUp() {
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
    public void setNodeDataDatesFromTransactionData() {
        TransactionData transactionData =
                TestUtils.createTransactionWithSpecificHash(TRANSACTION_ONE_HASH);
        transactionData.setAttachmentTime(new Date());
        transactionData.setTransactionConsensusUpdateTime(new Date(transactionData.getAttachmentTime().getTime() + HUNDRED_SECONDS_IN_MILLISECONDS));
        NodeData nodeData = new NodeData();
        liveViewService.setNodeDataDatesFromTransactionData(transactionData, nodeData);
        Assert.assertEquals(nodeData.getTccDuration(), HUNDRED_SECONDS);
    }

    @Test
    public void updateNodeStatus() {
        TransactionData transactionData = TestUtils.createTransactionWithSpecificHash(TRANSACTION_TWO_HASH);
        liveViewService.addNode(transactionData);
        liveViewService.updateNodeStatus(transactionData, TCC_CONFIRMED_STATUS);
        GraphData graphData = liveViewService.getFullGraph();
        Assert.assertEquals((int) graphData.nodes.get(0).getStatus(), TCC_CONFIRMED_STATUS);
    }
}
