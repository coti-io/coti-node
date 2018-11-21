package io.coti.nodemanager;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.database.Interfaces.IRocksDBConnector;
import io.coti.basenode.http.BaseNodeHttpStringConstants;
import io.coti.nodemanager.controllers.NodeController;
import io.coti.nodemanager.database.RocksDBConnector;
import io.coti.nodemanager.model.ActiveNode;
import io.coti.nodemanager.model.NodeHistory;
import io.coti.nodemanager.services.NodeManagementService;
import io.coti.nodemanager.services.interfaces.ITrustScoreService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class NodeControllerTest {
    @Value("localhost")
    private String nodeManagerIp;

    @Value("8888")
    private String nodeManagerHttpPort;

    @MockBean
    private IPropagationPublisher propagationPublisher;


    @MockBean
    private NetworkNodeCrypto networkNodeCrypto;

    @MockBean
    private NodeHistory nodeHistory;

    @MockBean(name = "RocksDBConnector")
    private RocksDBConnector dataBaseConnector;

    @MockBean
    private ITrustScoreService trustScoreService;

    @MockBean
    private ActiveNode activeNode;

    @Autowired
    private NodeManagementService nodeManagerServiceMock;

    @Bean
    public IRocksDBConnector getDataBaseConnector() {
        return new RocksDBConnector();
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddingNode() {
        when(networkNodeCrypto.verifySignature(any())).thenReturn(true);
        NodeController nodeController = new NodeController(nodeManagerServiceMock);
        Stream.of(NodeType.values()).forEach(nodeType -> {
            Hash nodeHash = new Hash(String.valueOf(nodeType.ordinal()));
            NetworkNodeData nodeToTest = new NetworkNodeData(nodeType, nodeManagerIp, nodeManagerHttpPort, nodeHash);
            nodeController.newNode(nodeToTest);
            Assert.assertTrue(" Node was not entered properly ", nodeManagerServiceMock.getAllNetworkData().isNodeExistsOnMemory(nodeToTest));
            nodeManagerServiceMock.getAllNetworkData().removeNode(nodeToTest);
        });
    }

    @Test
    public void testInvalidSignature() {
        when(networkNodeCrypto.verifySignature(any())).thenReturn(false);
        Hash nodeHash = new Hash("1");
        NodeManagementService nodeManagerServiceMock = new NodeManagementService(propagationPublisher, networkNodeCrypto,
                nodeHistory, dataBaseConnector, trustScoreService, activeNode);
        NodeController nodeController = new NodeController(nodeManagerServiceMock);

        NetworkNodeData nodeToTest = new NetworkNodeData(NodeType.FullNode, nodeManagerIp, nodeManagerHttpPort, nodeHash);
        ResponseEntity<String> responseEntity = nodeController.newNode(nodeToTest);
        Assert.assertTrue(" Http status wasn't correct ", HttpStatus.CONFLICT.equals(responseEntity.getStatusCode()));
        Assert.assertTrue("Http body wasn't correct",
                BaseNodeHttpStringConstants.VALIDATION_EXCEPTION_MESSAGE.equals(responseEntity.getBody()));
    }

}
