package io.coti.nodemanager.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.nodemanager.data.ActiveNodeData;
import io.coti.nodemanager.data.NetworkNodeStatus;
import io.coti.nodemanager.data.NodeHistoryData;
import io.coti.nodemanager.data.NodeNetworkDataTimestamp;
import io.coti.nodemanager.http.data.SingleNodeDetailsForWallet;
import io.coti.nodemanager.model.ActiveNodes;
import io.coti.nodemanager.model.NodeHistory;
import io.coti.nodemanager.services.interfaces.INodeManagementService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.coti.nodemanager.http.HttpStringConstants.NODE_ADDED_TO_NETWORK;

@Slf4j
@Service
public class NodeManagementService implements INodeManagementService {

    public static final String FULL_NODES_FOR_WALLET_KEY = "FullNodes";
    public static final String TRUST_SCORE_NODES_FOR_WALLET_KEY = "TrustScoreNodes";
    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private NodeHistory nodeHistory;
    @Autowired
    private ActiveNodes activeNodes;
    @Autowired
    private INetworkService networkService;
    @Value("${server.ip}")
    private String nodeManagerIp;
    @Value("${propagation.port}")
    private String propagationPort;

    @PostConstruct
    private void init() {
        networkService.setNodeManagerPropagationAddress("tcp://" + nodeManagerIp + ":" + propagationPort);
        propagationPublisher.init(propagationPort, NodeType.NodeManager);
    }

    public void propagateNetworkChanges() {
        log.info("Propagating network change");
        propagationPublisher.propagate(networkService.getNetworkData(), Arrays.asList(NodeType.FullNode, NodeType.ZeroSpendServer,
                NodeType.DspNode, NodeType.TrustScoreNode, NodeType.FinancialServer));
    }

    public ResponseEntity<String> addNode(NetworkNodeData networkNodeData) {
        try {
            networkService.validateNetworkNodeData(networkNodeData);
            if (networkService.isNodeExistsOnMemory(networkNodeData)) {
                boolean isUpdated = networkService.updateNetworkNode(networkNodeData);
                if (isUpdated) {
                    ActiveNodeData activeNodeData = activeNodes.getByHash(networkNodeData.getHash());
                    if (activeNodeData == null) {
                        log.error("Node {} wasn't found in activeNode table but was found in memory!", networkNodeData.getNodeHash());
                    }
                }
            } else {
                networkService.addNode(networkNodeData);
            }
            ActiveNodeData activeNodeData = new ActiveNodeData(networkNodeData.getHash(), networkNodeData);
            activeNodes.put(activeNodeData);
            insertToDB(networkNodeData, NetworkNodeStatus.ACTIVE);
            propagateNetworkChanges();
            Thread.sleep(3000); // a delay for other nodes to make changes with the newly added node
            return ResponseEntity.status(HttpStatus.OK).body(String.format(NODE_ADDED_TO_NETWORK, networkNodeData.getNodeHash()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    private void insertToDB(NetworkNodeData networkNodeData, NetworkNodeStatus nodeStatus) {
        modifyNodeInNodeHistory(networkNodeData, nodeStatus);
    }

    public void insertDeletedNodeRecord(NetworkNodeData networkNodeData) {
        modifyNodeInNodeHistory(networkNodeData, NetworkNodeStatus.INACTIVE);
    }

    private void modifyNodeInNodeHistory(NetworkNodeData networkNodeData, NetworkNodeStatus status) {
        NodeHistoryData dbNode = nodeHistory.getByHash(networkNodeData.getHash());
        NodeNetworkDataTimestamp nodeNetworkDataTimestamp =
                new NodeNetworkDataTimestamp(Instant.now(), networkNodeData);
        if (dbNode != null) {
            dbNode.setNodeStatus(status);
            log.debug("Node was updated in the db. node: {}", dbNode);
        } else {
            dbNode = new NodeHistoryData(status, networkNodeData.getNodeHash(), networkNodeData.getNodeType());
            log.debug("New node was inserted the db. node: {}", dbNode);
        }
        dbNode.getNodeHistory().add(nodeNetworkDataTimestamp);
        nodeHistory.put(dbNode);
    }

    @Override
    public Map<String, List<SingleNodeDetailsForWallet>> getNetworkDetailsForWallet() {
        Map<String, List<SingleNodeDetailsForWallet>> networkDetailsForWallet = new HashedMap<>();
        List<SingleNodeDetailsForWallet> fullNodesDetailsForWallet = networkService.getMapFromFactory(NodeType.FullNode).values().stream()
                .map(this::createSingleNodeDetailsForWallet)
                .collect(Collectors.toList());
        List<SingleNodeDetailsForWallet> trustScoreNodesDetailsForWallet = networkService.getMapFromFactory(NodeType.TrustScoreNode).values().stream()
                .map(this::createSingleNodeDetailsForWallet)
                .collect(Collectors.toList());
        networkDetailsForWallet.put(FULL_NODES_FOR_WALLET_KEY, fullNodesDetailsForWallet);
        networkDetailsForWallet.put(TRUST_SCORE_NODES_FOR_WALLET_KEY, trustScoreNodesDetailsForWallet);
        return networkDetailsForWallet;
    }

    private SingleNodeDetailsForWallet createSingleNodeDetailsForWallet(NetworkNodeData node) {
        if (NodeType.FullNode.equals(node.getNodeType())) {
            return new SingleNodeDetailsForWallet(node.getHash(), node.getHttpFullAddress(), node.getFeePercentage(), node.getTrustScore());
        }
        return new SingleNodeDetailsForWallet(node.getHash(), node.getHttpFullAddress(), null, null);
    }
}