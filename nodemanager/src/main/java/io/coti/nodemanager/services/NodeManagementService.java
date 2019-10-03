package io.coti.nodemanager.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.Hash;
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

import java.time.Instant;
import java.util.ArrayList;
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
    public static final String FINANCIAL_SERVER_FOR_WALLET_KEY = "FinancialServer";
    @Autowired
    private IPropagationPublisher propagationPublisher;
    @Autowired
    private NodeHistory nodeHistory;
    @Autowired
    private ActiveNodes activeNodes;
    @Autowired
    private INetworkService networkService;
    @Autowired
    private StakingService stakingService;

    @Value("${server.ip}")
    private String nodeManagerIp;
    @Value("${propagation.port}")
    private String propagationPort;

    @Override
    public void init() {
        networkService.setNodeManagerPropagationAddress("tcp://" + nodeManagerIp + ":" + propagationPort);
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
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
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

        Map<Hash, NetworkNodeData> fullNodesDetails = networkService.getMapFromFactory(NodeType.FullNode);
        Hash selectedNode = stakingService.selectNode(fullNodesDetails);
        List<SingleNodeDetailsForWallet> fullNodesDetailsForWallet;
        if( selectedNode != null) {
            NetworkNodeData networkNodeData = fullNodesDetails.get(selectedNode);
            fullNodesDetailsForWallet = fullNodesDetails.values().stream()
                    .map(this::createSingleNodeDetailsForWallet)
                    .filter(S -> stakingService.filterFullNodes(S))
                    .collect(Collectors.toList());
            SingleNodeDetailsForWallet selectedNodeForWallet = createSingleNodeDetailsForWallet(networkNodeData);
            fullNodesDetailsForWallet.remove(selectedNodeForWallet);
            fullNodesDetailsForWallet.add(0, createSingleNodeDetailsForWallet(networkNodeData));
        }
        else {
            fullNodesDetailsForWallet = fullNodesDetails.values().stream()
                    .map(this::createSingleNodeDetailsForWallet)
                    .filter(S -> stakingService.filterFullNodes(S))
                    .collect(Collectors.toList());
        }

        List<SingleNodeDetailsForWallet> trustScoreNodesDetailsForWallet = networkService.getMapFromFactory(NodeType.TrustScoreNode).values().stream()
                .map(this::createSingleNodeDetailsForWallet)
                .collect(Collectors.toList());
        List<SingleNodeDetailsForWallet> financialServerDetailsForWallet = new ArrayList<>();
        NetworkNodeData financialServer = networkService.getSingleNodeData(NodeType.FinancialServer);
        if (financialServer != null) {
            financialServerDetailsForWallet.add(createSingleNodeDetailsForWallet(financialServer));
        }
        networkDetailsForWallet.put(FULL_NODES_FOR_WALLET_KEY, fullNodesDetailsForWallet);
        networkDetailsForWallet.put(TRUST_SCORE_NODES_FOR_WALLET_KEY, trustScoreNodesDetailsForWallet);
        networkDetailsForWallet.put(FINANCIAL_SERVER_FOR_WALLET_KEY, financialServerDetailsForWallet);
        return networkDetailsForWallet;
    }

    @Override
    public SingleNodeDetailsForWallet getOneNodeDetailsForWallet() {
        Map<Hash, NetworkNodeData> fullNodesDetails = networkService.getMapFromFactory(NodeType.FullNode);
        Hash selectedNode = stakingService.selectNode(fullNodesDetails);
        if( selectedNode != null) {
            return createSingleNodeDetailsForWallet(fullNodesDetails.get(selectedNode));
        }
        else {
            return null;
        }
    }

    private SingleNodeDetailsForWallet createSingleNodeDetailsForWallet(NetworkNodeData node) {
        SingleNodeDetailsForWallet singleNodeDetailsForWallet = new SingleNodeDetailsForWallet(node.getHash(), node.getHttpFullAddress(), node.getWebServerUrl());
        if (NodeType.FullNode.equals(node.getNodeType())) {
            singleNodeDetailsForWallet.setFeeData(node.getFeeData());
            singleNodeDetailsForWallet.setTrustScore(node.getTrustScore());
        }
        return singleNodeDetailsForWallet;
    }
}