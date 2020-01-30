package io.coti.nodemanager.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.exceptions.NetworkNodeValidationException;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.nodemanager.data.*;
import io.coti.nodemanager.http.data.SingleNodeDetailsForWallet;
import io.coti.nodemanager.model.ActiveNodes;
import io.coti.nodemanager.model.NodeDailyActivities;
import io.coti.nodemanager.model.NodeHistory;
import io.coti.nodemanager.model.ReservedHosts;
import io.coti.nodemanager.services.interfaces.INodeManagementService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_NODE_SERVER_URL_HOST_RESERVED;
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
    private ReservedHosts reservedHosts;
    @Autowired
    private INetworkService networkService;
    @Autowired
    private StakingService stakingService;
    @Autowired
    private NodeDailyActivities nodeDailyActivities;
    @Autowired
    private NetworkHistoryService networkHistoryService;
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
            validateReservedHostToNode(networkNodeData);
            networkService.addNode(networkNodeData);
            ActiveNodeData activeNodeData = new ActiveNodeData(networkNodeData.getHash(), networkNodeData);
            activeNodes.put(activeNodeData);
            addNodeHistory(networkNodeData, NetworkNodeStatus.ACTIVE, Instant.now());
            propagateNetworkChanges();
            Thread.sleep(3000); // a delay for other nodes to make changes with the newly added node
            return ResponseEntity.status(HttpStatus.OK).body(String.format(NODE_ADDED_TO_NETWORK, networkNodeData.getNodeHash()));
        } catch (CotiRunTimeException e) {
            e.logMessage();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }

    }

    private void validateReservedHostToNode(NetworkNodeData networkNodeData) {
        if (networkNodeData.getNodeType().equals(NodeType.FullNode)) {
            String webServerUrl = networkNodeData.getWebServerUrl();
            String host = networkService.getHost(webServerUrl);

            Hash domainHash = calculateHostHash(host);
            ReservedHostData reservedHostData = reservedHosts.getByHash(domainHash);
            Hash nodeHash = networkNodeData.getNodeHash();
            if (reservedHostData == null) {
                reservedHosts.put(new ReservedHostData(domainHash, nodeHash));
            } else {
                if (!reservedHostData.getNodeHash().equals(nodeHash)) {
                    log.error("Invalid host. The host {} is already reserved to a different user", host);
                    throw new NetworkNodeValidationException(INVALID_NODE_SERVER_URL_HOST_RESERVED);
                }
            }
        }
    }

    private Hash calculateHostHash(String host) {
        return new Hash(host.getBytes(StandardCharsets.UTF_8));
    }

    public void addNodeHistory(NetworkNodeData networkNodeData, NetworkNodeStatus nodeStatus, Instant currentEventDateTime) {
        if (networkNodeData == null) {
            return;
        }
        Hash nodeHash = networkNodeData.getNodeHash();
        try {
            synchronized (addLockToLockMap(nodeHash)) {
                LocalDate currentEventDate = currentEventDateTime.atZone(ZoneId.of("UTC")).toLocalDate();
                NodeDailyActivityData nodeDailyActivityData = nodeDailyActivities.getByHash(nodeHash);
                if (nodeDailyActivityData == null) {
                    if (nodeStatus != NetworkNodeStatus.ACTIVE) {
                        return;
                    }
                    nodeDailyActivityData = new NodeDailyActivityData(nodeHash, networkNodeData.getNodeType());
                }
                NodeNetworkDataRecord newNodeNetworkDataRecord =
                        new NodeNetworkDataRecord(currentEventDateTime, nodeStatus, networkNodeData);
                addReferenceToNodeNetworkDataRecord(nodeDailyActivityData, nodeHash, newNodeNetworkDataRecord);
                Hash nodeHistoryDataHash = networkHistoryService.calculateNodeHistoryDataHash(nodeHash, currentEventDate);
                NodeHistoryData nodeHistoryData = nodeHistory.getByHash(nodeHistoryDataHash);
                if (nodeHistoryData == null) {
                    nodeHistoryData = new NodeHistoryData(nodeHistoryDataHash);
                }
                nodeHistoryData.getNodeNetworkDataRecordMap().put(newNodeNetworkDataRecord.getHash(), newNodeNetworkDataRecord);
                nodeHistory.put(nodeHistoryData);
                nodeDailyActivityData.getNodeDaySet().add(currentEventDate);
                nodeDailyActivities.put(nodeDailyActivityData);
            }
        } finally {
            removeLockFromLocksMap(nodeHash);
        }

    }

    private void addReferenceToNodeNetworkDataRecord(NodeDailyActivityData nodeDailyActivityData, Hash nodeHash, NodeNetworkDataRecord nodeNetworkDataRecord) {

        ConcurrentSkipListSet<LocalDate> nodeDaySet = nodeDailyActivityData.getNodeDaySet();
        if (nodeDaySet.isEmpty()) {
            return;
        }
        Pair<LocalDate, Hash> reference;

        LocalDate lastDate = nodeDailyActivityData.getNodeDaySet().last();
        Hash calculateNodeHistoryDataHash = networkHistoryService.calculateNodeHistoryDataHash(nodeHash, lastDate);
        NodeHistoryData nodeHistoryData = nodeHistory.getByHash(calculateNodeHistoryDataHash);
        NodeNetworkDataRecord lastNodeNetworkDataRecord = networkHistoryService.getLastNodeNetworkDataRecord(nodeHistoryData);
        if (lastNodeNetworkDataRecord.getNodeStatus().equals(NetworkNodeStatus.ACTIVE)) {
            NodeNetworkDataRecord previousNodeNetworkDataRecord = networkHistoryService.getNodeNetworkDataRecordByChainRef(lastNodeNetworkDataRecord);
            if (previousNodeNetworkDataRecord == null || !previousNodeNetworkDataRecord.getNodeStatus().equals(NetworkNodeStatus.ACTIVE)) {
                reference = new ImmutablePair<>(lastNodeNetworkDataRecord.getRecordTime().atZone(ZoneId.of("UTC")).toLocalDate(), lastNodeNetworkDataRecord.getHash());
            } else {
                reference = lastNodeNetworkDataRecord.getStatusChainRef();
            }
        } else {
            reference = new ImmutablePair<>(lastNodeNetworkDataRecord.getRecordTime().atZone(ZoneId.of("UTC")).toLocalDate(), lastNodeNetworkDataRecord.getHash());
        }
        nodeNetworkDataRecord.setStatusChainRef(reference);

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