package io.coti.nodemanager;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.crypto.NetworkNodeCrypto;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.nodemanager.data.ActiveNodeData;
import io.coti.nodemanager.data.NodeHistoryData;
import io.coti.nodemanager.data.NodeNetworkDataTimestamp;
import io.coti.nodemanager.data.SingleNodeDetailsForWallet;
import io.coti.nodemanager.database.NetworkNodeStatus;
import io.coti.nodemanager.database.RocksDBConnector;
import io.coti.nodemanager.model.ActiveNode;
import io.coti.nodemanager.model.NodeHistory;
import io.coti.nodemanager.services.NodeManagementService;
import io.coti.nodemanager.services.interfaces.INodeManagementService;
import io.coti.nodemanager.services.interfaces.ITrustScoreService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
@RunWith(SpringRunner.class)
public class NodeManagementServiceTest {

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

    @MockBean
    private ActiveNode activeNode;
    @Autowired
    private INodeManagementService nodeManagementService;

    @Test
    public void testUpdateNetworkChanges() {
        NetworkNodeData nodeToTest = new NetworkNodeData(NodeType.FullNode, nodeManagerIp, nodeManagerHttpPort, new Hash("1"));
        nodeManagementService.getAllNetworkData().addNode(nodeToTest);
        nodeManagementService.updateNetworkChanges();
        verify(propagationPublisher).propagate(Mockito.eq(nodeManagementService.getAllNetworkData()), any(List.class));
        nodeManagementService.getAllNetworkData().removeNode(nodeToTest);
    }

    @Test
    public void testInsertDeletedNodeRecord() {
        Hash nodeHash = new Hash("1");
        NodeType nodeType = NodeType.DspNode;
        NetworkNodeData nodeToTest = new NetworkNodeData(nodeType, nodeManagerIp, nodeManagerHttpPort, nodeHash);
        INodeManagementService nodeManagementServiceMock = spy(nodeManagementService);
        LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);
        when(nodeManagementServiceMock.getUTCnow()).thenReturn(localDateTime);
        nodeManagementServiceMock.getAllNetworkData().addNode(nodeToTest);
        nodeManagementServiceMock.insertDeletedNodeRecord(nodeToTest);
        NodeNetworkDataTimestamp nodeNetworkDataTimestamp = new NodeNetworkDataTimestamp(localDateTime, nodeToTest);
        NodeHistoryData inactiveDbNode = new NodeHistoryData(NetworkNodeStatus.INACTIVE, nodeHash, nodeType);
        inactiveDbNode.getNodeHistory().add(nodeNetworkDataTimestamp);
        verify(nodeHistory).put(inactiveDbNode);
    }

    @Test
    public void testFullAddNodeFlow() throws IllegalAccessException {
        Hash nodeHash = new Hash("1");
        NodeType nodeType = NodeType.TrustScoreNode;
        when(networkNodeCrypto.verifySignature(any())).thenReturn(true);
        NetworkNodeData nodeToTest = new NetworkNodeData(nodeType, nodeManagerIp, nodeManagerHttpPort, nodeHash);

        ActiveNodeData activeNodeData = new ActiveNodeData(nodeHash, nodeToTest);
        NodeHistoryData nodeHistoryData = new NodeHistoryData(NetworkNodeStatus.ACTIVE, nodeHash, nodeType);
        LocalDateTime localDateTime = LocalDateTime.now(ZoneOffset.UTC);
        NodeNetworkDataTimestamp nodeNetworkDataTimestamp = new NodeNetworkDataTimestamp(localDateTime, nodeToTest);
        nodeHistoryData.getNodeHistory().add(nodeNetworkDataTimestamp);
        INodeManagementService nodeManagementServiceMock = spy(nodeManagementService);
        when(nodeManagementServiceMock.getUTCnow()).thenReturn(localDateTime);
        nodeManagementServiceMock.newNode(nodeToTest);
        verify(activeNode).put(activeNodeData);
        verify(nodeHistory).put(nodeHistoryData);

    }

    @Test
    public void testFullModifyNode() throws IllegalAccessException {
        Hash nodeHash = new Hash("2");
        when(networkNodeCrypto.verifySignature(any())).thenReturn(true);
        NodeType nodeType = NodeType.DspNode;
        NetworkNodeStatus nodeStatus = NetworkNodeStatus.ACTIVE;
        NetworkNodeData nodeToTest = new NetworkNodeData(nodeType, nodeManagerIp, nodeManagerHttpPort, nodeHash);
        NodeNetworkDataTimestamp nodeNetworkDataTimestamp = new NodeNetworkDataTimestamp(LocalDateTime.now(ZoneOffset.UTC), nodeToTest);
        NodeHistoryData activeNodeHistoryData = new NodeHistoryData(nodeStatus, nodeToTest.getNodeHash(), nodeToTest.getNodeType());
        NodeHistoryData activeNodeHistorySpy = spy(activeNodeHistoryData);
        activeNodeHistorySpy.getNodeHistory().add(nodeNetworkDataTimestamp);
        when(nodeHistory.getByHash(nodeToTest.getHash())).thenReturn(activeNodeHistorySpy);
        nodeManagementService.newNode(nodeToTest);
        verify(activeNodeHistorySpy).setNodeStatus(nodeStatus);
        verify(nodeHistory).put(activeNodeHistorySpy);
        Assert.assertTrue(activeNodeHistorySpy.getNodeHistory().size() == 2);
        Assert.assertTrue(nodeManagementService.getAllNetworkData().isNodeExistsOnMemory(nodeToTest));
        verify(propagationPublisher).propagate(Mockito.eq(nodeManagementService.getAllNetworkData()), any(List.class));
    }

    @Test
    public void testCreateNetworkDetailsForWallet() {
        NetworkNodeData dspNode = new NetworkNodeData(NodeType.DspNode, nodeManagerIp, nodeManagerHttpPort, createDummyHash());

        NetworkNodeData fullNode = new NetworkNodeData(NodeType.FullNode, nodeManagerIp, nodeManagerHttpPort, createDummyHash());
        NetworkNodeData fullNode2 = new NetworkNodeData(NodeType.FullNode, nodeManagerIp, nodeManagerHttpPort, createDummyHash());

        NetworkNodeData trustScoreNode = new NetworkNodeData(NodeType.TrustScoreNode, nodeManagerIp, nodeManagerHttpPort, createDummyHash());
        NetworkNodeData trustScoreNode2 = new NetworkNodeData(NodeType.TrustScoreNode, nodeManagerIp, nodeManagerHttpPort, createDummyHash());

        NetworkNodeData zeroSpendNode = new NetworkNodeData(NodeType.ZeroSpendServer, nodeManagerIp, nodeManagerHttpPort, createDummyHash());

        nodeManagementService.getAllNetworkData().addNode(dspNode);
        nodeManagementService.getAllNetworkData().addNode(fullNode);
        nodeManagementService.getAllNetworkData().addNode(fullNode2);
        nodeManagementService.getAllNetworkData().addNode(trustScoreNode);
        nodeManagementService.getAllNetworkData().addNode(trustScoreNode2);
        nodeManagementService.getAllNetworkData().addNode(zeroSpendNode);

        Map<String, List<SingleNodeDetailsForWallet>> walletInfoMap = nodeManagementService.createNetworkDetailsForWallet();
        Assert.assertTrue(walletInfoMap.keySet().size() == 2);
        List<SingleNodeDetailsForWallet> fullNodes = walletInfoMap.get(NodeManagementService.FULL_NODES_FORWALLET_KEY);
        Assert.assertTrue(fullNodes.size() == 2);
        Assert.assertTrue(fullNode.getHttpFullAddress().equals(fullNodes.get(0).getFullHttpAddress()));
        List<SingleNodeDetailsForWallet> trustScoreNodes = walletInfoMap.get(NodeManagementService.TRUST_SCORE_NODES_FORWALLET_KEY);
        Assert.assertTrue(trustScoreNodes.size() == 2);
        Assert.assertTrue(fullNode.getHttpFullAddress().equals(trustScoreNodes.get(0).getFullHttpAddress()));
    }

    private Hash createDummyHash() {
        return new Hash("123");
    }


}
