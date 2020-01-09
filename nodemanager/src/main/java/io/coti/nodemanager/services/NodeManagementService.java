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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
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
            addNodeHistory(networkNodeData, NetworkNodeStatus.ACTIVE, Instant.now());
            propagateNetworkChanges();
            Thread.sleep(3000); // a delay for other nodes to make changes with the newly added node
            return ResponseEntity.status(HttpStatus.OK).body(String.format(NODE_ADDED_TO_NETWORK, networkNodeData.getNodeHash()));
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    public void addNodeHistory(NetworkNodeData networkNodeData, NetworkNodeStatus nodeStatus, Instant currentEventDateTime) {
        if (networkNodeData == null) {
            return;
        }
        Hash nodeHash = networkNodeData.getNodeHash();
        try {
            synchronized (addLockToLockMap(nodeHash)) {
                LocalDate currentEventDate = currentEventDateTime.atZone(ZoneId.of("UTC")).toLocalDate();
                NodeDayMapData nodeDayMapsByHash = nodeDayMaps.getByHash(nodeHash);
                if (nodeDayMapsByHash == null) {
                    addNodeHistoryInitNewNode(networkNodeData, nodeStatus, nodeHash, currentEventDateTime);
                } else {
                    LocalDate lastDateInNodeDayMap = nodeDayMapsByHash.getNodeDaySet().last();
                    if (lastDateInNodeDayMap.isBefore(currentEventDate)) {
                        addNodeHistoryEventInNewDate(nodeDayMapsByHash, currentEventDateTime, nodeStatus, networkNodeData);
                    } else {
                        addNodeHistoryExistingDateNewEntry(networkNodeData, nodeStatus, currentEventDateTime, nodeDayMapsByHash);
                    }
                }
            }
        } finally {
            removeLockFromLocksMap(nodeHash);
        }

    }

    private void addNodeHistoryInitNewNode(NetworkNodeData networkNodeData, NetworkNodeStatus nodeStatus,
                                           Hash nodeHash, Instant currentEventDateTime) {
        NodeDayMapData nodeDayMapsByHash = new NodeDayMapData(nodeHash);
        LocalDate currentEventDate = currentEventDateTime.atZone(ZoneId.of("UTC")).toLocalDate();
        Hash calculateNodeHistoryDataHash = nodeDayMapsByHash.calculateNodeHistoryDataHash(currentEventDate);
        nodeDayMapsByHash.getNodeDaySet().add(currentEventDate);
        NodeHistoryData newNodeHistoryByHash = addNodeHistoryPutNodeNetworkDataRecordForNewDate(currentEventDateTime, nodeStatus, networkNodeData, calculateNodeHistoryDataHash);
        nodeHistory.put(newNodeHistoryByHash);

        Hash nodeHistoryLastEntryHash = newNodeHistoryByHash.getNodeHistory().lastKey();
        Pair<LocalDate, Hash> pair = Pair.of(currentEventDate, nodeHistoryLastEntryHash);

        getNodeCurrentNetworkDataRecord(nodeDayMapsByHash).setStatusChainRef(pair);
        nodeDayMaps.put(nodeDayMapsByHash);
        log.debug("New node was inserted the db. node: {}", nodeHash);
    }

    private NodeHistoryData addNodeHistoryPutNodeNetworkDataRecordForNewDate(Instant currentEventDateTime, NetworkNodeStatus nodeStatus, NetworkNodeData networkNodeData, Hash newEventDateCalculatedNodeDateHash) {
        NodeHistoryData newNodeHistoryByHash = new NodeHistoryData(newEventDateCalculatedNodeDateHash);
        NodeNetworkDataRecord newNodeNetworkDataRecord =
                new NodeNetworkDataRecord(currentEventDateTime, nodeStatus, networkNodeData);
        newNodeHistoryByHash.getNodeHistory().put(newNodeNetworkDataRecord.getHash(), newNodeNetworkDataRecord);
        return newNodeHistoryByHash;
    }

    private void addNodeHistoryEventInNewDate(NodeDayMapData nodeDayMapsByHash, Instant currentEventDateTime,
                                              NetworkNodeStatus nodeStatus, NetworkNodeData networkNodeData) {
        LocalDate currentEventDate = currentEventDateTime.atZone(ZoneId.of("UTC")).toLocalDate();
        Hash newEventDateCalculatedNodeDateHash = nodeDayMapsByHash.calculateNodeHistoryDataHash(currentEventDate);
        NodeHistoryData newNodeHistoryByHash = addNodeHistoryPutNodeNetworkDataRecordForNewDate(currentEventDateTime, nodeStatus, networkNodeData, newEventDateCalculatedNodeDateHash);
        nodeHistory.put(newNodeHistoryByHash);

        addNodeHistoryUpdateNodeDayMap(nodeDayMapsByHash, nodeStatus, currentEventDate, newNodeHistoryByHash);
    }

    private void addNodeHistoryUpdateNodeDayMap(NodeDayMapData nodeDayMapsByHash, NetworkNodeStatus nodeStatus, LocalDate currentEventDate, NodeHistoryData existingEventDateNodeHistoryData) {
        Pair<LocalDate, Hash> pair;

        NetworkNodeStatus previousNodeStatus = getNodeCurrentStatus(nodeDayMapsByHash);
        boolean isChainHead = isChainHead(nodeDayMapsByHash, previousNodeStatus);

        boolean isSameStatus = nodeStatus == previousNodeStatus;
        if (isChainHead && isSameStatus) {
            LocalDate lastDateWithEvent = nodeDayMapsByHash.getNodeDaySet().last();
            NodeHistoryData nodeHistoryLastDate = nodeHistory.getByHash(nodeDayMapsByHash.calculateNodeHistoryDataHash(lastDateWithEvent));
            LinkedMap<Hash, NodeNetworkDataRecord> lastDateWithEventsNodeHistory = nodeHistoryLastDate.getNodeHistory();
            Hash previousNodeNetworkDataRecordHash = lastDateWithEventsNodeHistory.lastKey();
            pair = Pair.of(lastDateWithEvent, previousNodeNetworkDataRecordHash);
        } else {
            pair = getNodeCurrentNetworkDataRecord(nodeDayMapsByHash).getStatusChainRef();
        }
        getNodeCurrentNetworkDataRecord(nodeDayMapsByHash).setStatusChainRef(pair);
        nodeDayMapsByHash.getNodeDaySet().add(currentEventDate);
        nodeDayMaps.put(nodeDayMapsByHash);
        existingEventDateNodeHistoryData.getNodeHistory().get(existingEventDateNodeHistoryData.getNodeHistory().lastKey()).setStatusChainRef(pair);
        nodeHistory.put(existingEventDateNodeHistoryData);
    }

    private boolean isChainHead(NodeDayMapData nodeDayMapsByHash, NetworkNodeStatus previousNodeStatus) {
        NodeNetworkDataRecord previousNodeNetworkDataRecord = getNodeCurrentNetworkDataRecord(nodeDayMapsByHash);
        Pair<LocalDate, Hash> previousStatusChainRef = previousNodeNetworkDataRecord.getStatusChainRef();
        NodeNetworkDataRecord previousTwiceNodeNetworkDataRecord = getNodeNetworkDataRecordByChainRef(nodeDayMapsByHash, previousStatusChainRef);
        NetworkNodeStatus previousTwiceNodeStatus = previousTwiceNodeNetworkDataRecord.getNodeStatus();
        return previousNodeStatus != previousTwiceNodeStatus;
    }

    protected NodeNetworkDataRecord getNodeNetworkDataRecordByChainRef(NodeDayMapData nodeDayMapsByHash, Pair<LocalDate, Hash> chainRef) {
        return nodeHistory.getByHash(nodeDayMapsByHash.calculateNodeHistoryDataHash(chainRef.getLeft())).getNodeHistory().get(chainRef.getRight());
    }

    private NodeNetworkDataRecord getNodeCurrentNetworkDataRecord(NodeDayMapData nodeDayMapsByHash) {
        NodeHistoryData nodeHistoryData = nodeHistory.getByHash(nodeDayMapsByHash.calculateNodeHistoryDataHash(nodeDayMapsByHash.getNodeDaySet().last()));
        return nodeHistoryData.getNodeHistory().get(nodeHistoryData.getNodeHistory().lastKey());
    }

    private NetworkNodeStatus getNodeCurrentStatus(NodeDayMapData nodeDayMapsByHash) {
        NodeNetworkDataRecord nodeNetworkDataRecord = getNodeCurrentNetworkDataRecord(nodeDayMapsByHash);
        return nodeNetworkDataRecord.getNodeStatus();
    }

    private void addNodeHistoryExistingDateNewEntry(NetworkNodeData networkNodeData, NetworkNodeStatus nodeStatus, Instant currentEventDateTime,
                                                    NodeDayMapData nodeDayMapsByHash) {
        LocalDate currentEventDate = currentEventDateTime.atZone(ZoneId.of("UTC")).toLocalDate();
        Hash existingEventDateCalculatedNodeDateHash = nodeDayMapsByHash.calculateNodeHistoryDataHash(nodeDayMapsByHash.getNodeDaySet().last());
        NodeHistoryData existingEventDateNodeHistoryData = nodeHistory.getByHash(existingEventDateCalculatedNodeDateHash);
        NodeNetworkDataRecord newNodeNetworkDataRecord = new NodeNetworkDataRecord(currentEventDateTime, nodeStatus, networkNodeData);
        existingEventDateNodeHistoryData.getNodeHistory().put(newNodeNetworkDataRecord.getHash(), newNodeNetworkDataRecord);

        addNodeHistoryUpdateNodeDayMap(nodeDayMapsByHash, nodeStatus, currentEventDate, existingEventDateNodeHistoryData);
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