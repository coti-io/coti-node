package io.coti.nodemanager.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.nodemanager.data.*;
import io.coti.nodemanager.http.data.SingleNodeDetailsForWallet;
import io.coti.nodemanager.model.ActiveNodes;
import io.coti.nodemanager.model.NodeDayMaps;
import io.coti.nodemanager.model.NodeHistory;
import io.coti.nodemanager.services.interfaces.INodeManagementService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    @Autowired
    private NodeDayMaps nodeDayMaps;


    @Value("${server.ip}")
    private String nodeManagerIp;
    @Value("${propagation.port}")
    private String propagationPort;

    private Map<Hash, Hash> lockNodeHistoryRecordHashMap = new ConcurrentHashMap<>();

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
            addNodeHistory(networkNodeData, NetworkNodeStatus.ACTIVE, LocalDateTime.now(ZoneOffset.UTC));
            propagateNetworkChanges();
            Thread.sleep(3000); // a delay for other nodes to make changes with the newly added node
            return ResponseEntity.status(HttpStatus.OK).body(String.format(NODE_ADDED_TO_NETWORK, networkNodeData.getNodeHash()));
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    public void addNodeHistory(NetworkNodeData networkNodeData, NetworkNodeStatus nodeStatus, LocalDateTime currentEventDateTime) {
        if (networkNodeData == null) {
            return;
        }
        Hash nodeHash = networkNodeData.getNodeHash();
        try {
            synchronized (addLockToLockMap(nodeHash)) {
                LocalDate currentEventDate = currentEventDateTime.toLocalDate();
                NodeDayMapData nodeDayMapsByHash = nodeDayMaps.getByHash(nodeHash);
                if (nodeDayMapsByHash == null) {
                    writeNodeHistoryInitNewNode(networkNodeData, nodeStatus, nodeHash, currentEventDateTime);
                } else {
                    LocalDate lastDateInNodeDayMap = nodeDayMapsByHash.getNodeDayMap().lastKey();
                    if (lastDateInNodeDayMap.isBefore(currentEventDate)) {
                        writeNodeHistoryEventInNewDate(nodeDayMapsByHash, currentEventDateTime, nodeStatus, networkNodeData);
                    } else {
                        writeNodeHistoryExistingDateNewEntry(networkNodeData, nodeStatus, currentEventDateTime, nodeDayMapsByHash);
                    }
                }
            }
        } finally {
            removeLockFromLocksMap(nodeHash);
        }

    }

    private void writeNodeHistoryInitNewNode(NetworkNodeData networkNodeData, NetworkNodeStatus nodeStatus,
                                             Hash nodeHash, LocalDateTime currentEventDateTime) {
        NodeDayMapData nodeDayMapsByHash = new NodeDayMapData(nodeHash, currentEventDateTime);
        LocalDate currentEventDate = currentEventDateTime.toLocalDate();
        Hash calculateNodeHistoryDataHash = nodeDayMapsByHash.calculateNodeHistoryDataHash(currentEventDate);
        nodeDayMapsByHash.getNodeDayMap().put(currentEventDate, calculateNodeHistoryDataHash);
        NodeHistoryData newNodeHistoryByHash = writeNodeHistoryPutNodeNetworkDataRecordForNewDate(currentEventDateTime, nodeStatus, networkNodeData, calculateNodeHistoryDataHash);
        nodeHistory.put(newNodeHistoryByHash);

        Hash nodeHistoryLastEntryHash = newNodeHistoryByHash.getNodeHistory().lastKey();
        Pair<LocalDate, Hash> pair = Pair.of(currentEventDate, nodeHistoryLastEntryHash);

        nodeDayMapsByHash.setStatusChainRef(pair);
        nodeDayMapsByHash.setNodeStatus(nodeStatus);
        nodeDayMapsByHash.setChainHead(false);
        nodeDayMaps.put(nodeDayMapsByHash);
        log.debug("New node was inserted the db. node: {}", nodeHash);
    }

    private NodeHistoryData writeNodeHistoryPutNodeNetworkDataRecordForNewDate(LocalDateTime currentEventDateTime, NetworkNodeStatus nodeStatus, NetworkNodeData networkNodeData, Hash newEventDateCalculatedNodeDateHash) {
        NodeHistoryData newNodeHistoryByHash = new NodeHistoryData(newEventDateCalculatedNodeDateHash);
        NodeNetworkDataRecord newNodeNetworkDataRecord =
                new NodeNetworkDataRecord(currentEventDateTime, nodeStatus, networkNodeData);
        newNodeHistoryByHash.getNodeHistory().put(newNodeNetworkDataRecord.getHash(), newNodeNetworkDataRecord);
        return newNodeHistoryByHash;
    }

    private void writeNodeHistoryEventInNewDate(NodeDayMapData nodeDayMapsByHash, LocalDateTime currentEventDateTime,
                                                NetworkNodeStatus nodeStatus, NetworkNodeData networkNodeData) {
        LocalDate currentEventDate = currentEventDateTime.toLocalDate();
        Hash newEventDateCalculatedNodeDateHash = nodeDayMapsByHash.calculateNodeHistoryDataHash(currentEventDate);
        NodeHistoryData newNodeHistoryByHash = writeNodeHistoryPutNodeNetworkDataRecordForNewDate(currentEventDateTime, nodeStatus, networkNodeData, newEventDateCalculatedNodeDateHash);
        nodeHistory.put(newNodeHistoryByHash);

        writeNodeHistoryUpdateNodeDayMap(nodeDayMapsByHash, nodeStatus, currentEventDate, newNodeHistoryByHash);
    }

    private void writeNodeHistoryUpdateNodeDayMap(NodeDayMapData nodeDayMapsByHash, NetworkNodeStatus nodeStatus, LocalDate currentEventDate, NodeHistoryData existingEventDateNodeHistoryData) {
        Pair<LocalDate, Hash> pair;

        boolean isSameStatus = nodeStatus == nodeDayMapsByHash.getNodeStatus();
        if (nodeDayMapsByHash.isChainHead() && isSameStatus) {
            Map.Entry<LocalDate, Hash> lastDateWithEventEntry = nodeDayMapsByHash.getNodeDayMap().lastEntry();
            NodeHistoryData nodeHistoryLastDate = nodeHistory.getByHash(lastDateWithEventEntry.getValue());
            LinkedMap<Hash, NodeNetworkDataRecord> lastDateWithEventsNodeHistory = nodeHistoryLastDate.getNodeHistory();
            Hash previousNodeNetworkDataRecordHash = lastDateWithEventsNodeHistory.lastKey();
            pair = Pair.of(lastDateWithEventEntry.getKey(), previousNodeNetworkDataRecordHash);
        } else {
            pair = nodeDayMapsByHash.getStatusChainRef();
        }
        nodeDayMapsByHash.setChainHead(!isSameStatus);
        nodeDayMapsByHash.setNodeStatus(nodeStatus);
        nodeDayMapsByHash.setStatusChainRef(pair);
        nodeDayMapsByHash.getNodeDayMap().putIfAbsent(currentEventDate, nodeDayMapsByHash.calculateNodeHistoryDataHash(currentEventDate));
        nodeDayMaps.put(nodeDayMapsByHash);
        existingEventDateNodeHistoryData.getNodeHistory().get(existingEventDateNodeHistoryData.getNodeHistory().lastKey()).setStatusChainRef(pair);
        nodeHistory.put(existingEventDateNodeHistoryData);
    }

    private void writeNodeHistoryExistingDateNewEntry(NetworkNodeData networkNodeData, NetworkNodeStatus nodeStatus, LocalDateTime currentEventDateTime,
                                                      NodeDayMapData nodeDayMapsByHash) {
        LocalDate currentEventDate = currentEventDateTime.toLocalDate();
        Hash existingEventDateCalculatedNodeDateHash = nodeDayMapsByHash.getNodeDayMap().lastEntry().getValue();
        NodeHistoryData existingEventDateNodeHistoryData = nodeHistory.getByHash(existingEventDateCalculatedNodeDateHash);
        NodeNetworkDataRecord newNodeNetworkDataRecord = new NodeNetworkDataRecord(currentEventDateTime, nodeStatus, networkNodeData);
        existingEventDateNodeHistoryData.getNodeHistory().put(newNodeNetworkDataRecord.getHash(), newNodeNetworkDataRecord);

        writeNodeHistoryUpdateNodeDayMap(nodeDayMapsByHash, nodeStatus, currentEventDate, existingEventDateNodeHistoryData);
    }

    @Override
    public Map<String, List<SingleNodeDetailsForWallet>> getNetworkDetailsForWallet() {
        Map<String, List<SingleNodeDetailsForWallet>> networkDetailsForWallet = new HashedMap<>();

        Map<Hash, NetworkNodeData> fullNodesDetails = networkService.getMapFromFactory(NodeType.FullNode);
        NetworkNodeData selectedNode = stakingService.selectStakedNode(fullNodesDetails);
        List<SingleNodeDetailsForWallet> fullNodesDetailsForWallet = fullNodesDetails.values().stream()
                .map(this::createSingleNodeDetailsForWallet)
                .filter(singleNodeDetailsForWallet -> stakingService.filterFullNode(singleNodeDetailsForWallet))
                .collect(Collectors.toList());
        if (selectedNode != null) {
            SingleNodeDetailsForWallet selectedNodeForWallet = createSingleNodeDetailsForWallet(selectedNode);
            fullNodesDetailsForWallet.remove(selectedNodeForWallet);
            fullNodesDetailsForWallet.add(0, createSingleNodeDetailsForWallet(selectedNode));
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
        NetworkNodeData selectedNode = stakingService.selectStakedNode(fullNodesDetails);
        if (selectedNode != null) {
            return createSingleNodeDetailsForWallet(selectedNode);
        } else {
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

    protected Hash addLockToLockMap(Hash hash) {
        return addLockToLockMap(lockNodeHistoryRecordHashMap, hash);
    }

    private Hash addLockToLockMap(Map<Hash, Hash> locksIdentityMap, Hash hash) {
        synchronized (locksIdentityMap) {
            locksIdentityMap.putIfAbsent(hash, hash);
            return locksIdentityMap.get(hash);
        }
    }

    protected void removeLockFromLocksMap(Hash hash) {
        removeLockFromLocksMap(lockNodeHistoryRecordHashMap, hash);
    }

    private void removeLockFromLocksMap(Map<Hash, Hash> locksIdentityMap, Hash hash) {
        synchronized (locksIdentityMap) {
            Hash hashLock = locksIdentityMap.get(hash);
            if (hashLock != null) {
                locksIdentityMap.remove(hash);
            }
        }
    }

}