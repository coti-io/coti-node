package io.coti.nodemanager.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.data.NodeTypeName;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.nodemanager.data.*;
import io.coti.nodemanager.http.AddNodeBeginEventPairAdminRequest;
import io.coti.nodemanager.http.AddNodeEventAdminRequest;
import io.coti.nodemanager.http.data.SingleNodeDetailsForWallet;
import io.coti.nodemanager.model.ActiveNodes;
import io.coti.nodemanager.model.NodeDailyActivities;
import io.coti.nodemanager.model.NodeHistory;
import io.coti.nodemanager.services.interfaces.INodeManagementService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.nodemanager.http.HttpStringConstants.*;

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
            networkService.addNode(networkNodeData);
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
                NodeDailyActivityData nodeDailyActivityData = nodeDailyActivities.getByHash(nodeHash);
                if (nodeDailyActivityData == null) {
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
    public ResponseEntity<IResponse> addNodeBeginEventPairAdmin(AddNodeBeginEventPairAdminRequest request) {

        AddNodeEventAdminRequest addNodeEventAdminRequest;

        LocalDateTime localDateTimeForSecondEvent = findVeryFirstActiveEvent(request.getNodeHash());

        if (localDateTimeForSecondEvent == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                    body(new Response(String.format(ADDING_EVENT_PAIR_FAILED, request.getNodeHash())));

        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTimeEvent = LocalDateTime.parse(request.getStartDateTimeUTC(), formatter);
        if (!localDateTimeForSecondEvent.isAfter(localDateTimeEvent)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                    body(new Response(String.format(ADDING_EVENT_PAIR_FAILED, request.getNodeHash())));
        }

        addNodeEventAdminRequest = new AddNodeEventAdminRequest(request.getNodeHash(), request.getStartDateTimeUTC(), NodeTypeName.FullNode.toString(), NetworkNodeStatus.ACTIVE.toString());
        ResponseEntity<IResponse> response1 = addNodeEventAdmin(addNodeEventAdminRequest);
        if (response1.getStatusCode() != HttpStatus.OK){
            return response1;
        }

        addNodeEventAdminRequest = new AddNodeEventAdminRequest(request.getNodeHash(), formatter.format(localDateTimeForSecondEvent), NodeTypeName.FullNode.toString(), NetworkNodeStatus.INACTIVE.toString());
        return addNodeEventAdmin(addNodeEventAdminRequest);

    }

    private LocalDateTime findVeryFirstActiveEvent(Hash nodeHash){
        NodeDailyActivityData nodeDailyActivityData = nodeDailyActivities.getByHash(nodeHash);
        if (nodeDailyActivityData == null) {
            return null;
        }

        LocalDate localDate = nodeDailyActivityData.getNodeDaySet().first();

        NodeHistoryData nodeHistoryData = null;
        while(nodeHistoryData == null){
            Hash nodeHistoryDataHash = networkHistoryService.calculateNodeHistoryDataHash(nodeHash, localDate);
            nodeHistoryData = nodeHistory.getByHash(nodeHistoryDataHash);

            if (nodeHistoryData != null && !nodeHistoryData.getNodeNetworkDataRecordMap().isEmpty()) {
                Hash nodeNetworkDataRecordHash = nodeHistoryData.getNodeNetworkDataRecordMap().firstKey();
                NodeNetworkDataRecord nodeNetworkDataRecord = nodeHistoryData.getNodeNetworkDataRecordMap().get(nodeNetworkDataRecordHash);
                if (nodeNetworkDataRecord.getNodeStatus() != NetworkNodeStatus.ACTIVE) {
                    return null;
                } else {
                    return nodeNetworkDataRecord.getRecordTime().atZone(ZoneId.of("UTC")).toLocalDateTime();
                }
            } else {
                localDate = nodeDailyActivityData.getNodeDaySet().higher(localDate);
                if (localDate == null) {
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public ResponseEntity<IResponse> addNodeEventAdmin(AddNodeEventAdminRequest request) {

        Hash nodeHash = request.getNodeHash();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTimeEvent = LocalDateTime.parse(request.getRecordDateTimeUTC(), formatter);
        Instant instantDateTimeEvent = localDateTimeEvent.toInstant(ZoneOffset.UTC);
        LocalDate localDateForEvent = localDateTimeEvent.toLocalDate();

        NetworkNodeData networkNodeData = new NetworkNodeData();
        networkNodeData.setHash(nodeHash);
        networkNodeData.setNodeType(NodeTypeName.getNodeType(request.getNodeType()));

        NetworkNodeStatus nodeStatusForEvent = NetworkNodeStatus.enumFromString(request.getNodeStatus());
        NodeNetworkDataRecord newNodeNetworkDataRecord =
                new NodeNetworkDataRecord(instantDateTimeEvent, nodeStatusForEvent, networkNodeData);

        try {
            synchronized (addLockToLockMap(nodeHash)) {
                NodeDailyActivityData nodeDailyActivityData = nodeDailyActivities.getByHash(nodeHash);
                if (nodeDailyActivityData == null) {
                    nodeDailyActivityData = new NodeDailyActivityData(nodeHash, networkNodeData.getNodeType());
                }
                if (nodeDailyActivityData.getNodeDaySet().add(localDateForEvent)) {
                    nodeDailyActivities.put(nodeDailyActivityData);
                }

                Hash nodeHistoryDataHashForEvent = networkHistoryService.calculateNodeHistoryDataHash(nodeHash, localDateForEvent);
                NodeHistoryData nodeHistoryDataForEvent = nodeHistory.getByHash(nodeHistoryDataHashForEvent);
                if (nodeHistoryDataForEvent == null) {
                    nodeHistoryDataForEvent = new NodeHistoryData(nodeHistoryDataHashForEvent);
                }
                nodeHistoryDataForEvent.setNodeNetworkDataRecordMap(reCreateLinkedMapNodeNetworkDataRecord(nodeHistoryDataForEvent.getNodeNetworkDataRecordMap(), newNodeNetworkDataRecord));

                Pair<LocalDate, Hash> reference = findReferenceToPreviousEvent(nodeDailyActivityData, nodeHistoryDataForEvent, newNodeNetworkDataRecord, localDateForEvent, instantDateTimeEvent);
                newNodeNetworkDataRecord.setStatusChainRef(reference);

                if (nodeStatusForEvent == NetworkNodeStatus.INACTIVE || reference == null) {
                    reference = new ImmutablePair<>(localDateForEvent, newNodeNetworkDataRecord.getHash());
                }
                Pair<LocalDate, Hash> keepReference = reference;

                if (nodeStatusForEvent == NetworkNodeStatus.INACTIVE){
                    reference = updateNodeHistoryDataForDateForINACTIVE(nodeHistoryDataForEvent, newNodeNetworkDataRecord, reference, true, localDateForEvent);
                } else {
                    reference = updateNodeHistoryDataForDateForACTIVE(nodeHistoryDataForEvent, newNodeNetworkDataRecord, reference, true);
                }
                nodeHistory.put(nodeHistoryDataForEvent);

                LocalDate localDate = nodeDailyActivityData.getNodeDaySet().higher(localDateForEvent);
                NodeHistoryData nodeHistoryData;
                while (reference != null && localDate != null) {
                    Hash nodeHistoryDataHash = networkHistoryService.calculateNodeHistoryDataHash(nodeHash, localDate);
                    nodeHistoryData = nodeHistory.getByHash(nodeHistoryDataHash);
                    if (nodeHistoryData != null) {
                        if (nodeStatusForEvent == NetworkNodeStatus.INACTIVE && keepReference == reference){
                            reference = updateNodeHistoryDataForDateForINACTIVE(nodeHistoryData, newNodeNetworkDataRecord, reference, false, localDate);
                        } else {
                            reference = updateNodeHistoryDataForDateForACTIVE(nodeHistoryData, newNodeNetworkDataRecord, reference, false);
                        }
                        nodeHistory.put(nodeHistoryData);
                    }
                    localDate = nodeDailyActivityData.getNodeDaySet().higher(localDate);
                }

                return ResponseEntity.status(HttpStatus.OK).
                        body(new Response(String.format(NODE_HISTORY_RECORD_HAS_BEEN_ADDED_MANUALLY, nodeHash)));
            }

        } catch (Exception e) {
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(e.getMessage(), STATUS_ERROR));
        } finally {
            removeLockFromLocksMap(nodeHash);
        }
    }

    private Pair<LocalDate, Hash> findReferenceToPreviousEvent(NodeDailyActivityData nodeDailyActivityData, NodeHistoryData eventNodeHistoryData,
                                                               NodeNetworkDataRecord newNodeNetworkDataRecord, LocalDate eventDate, Instant eventDateTime) {

        Hash nodeNetworkDataRecordHash = eventNodeHistoryData.getNodeNetworkDataRecordMap().previousKey(newNodeNetworkDataRecord.getHash());
        NodeNetworkDataRecord nodeNetworkDataRecord = null;

        if (nodeNetworkDataRecordHash != null) {
            nodeNetworkDataRecord = eventNodeHistoryData.getNodeNetworkDataRecordMap().get(nodeNetworkDataRecordHash);
        }
        while (nodeNetworkDataRecordHash != null && nodeNetworkDataRecord.getRecordTime().isAfter(eventDateTime)) {
            nodeNetworkDataRecordHash = eventNodeHistoryData.getNodeNetworkDataRecordMap().previousKey(nodeNetworkDataRecordHash);
            nodeNetworkDataRecord = eventNodeHistoryData.getNodeNetworkDataRecordMap().get(nodeNetworkDataRecordHash);
        }
        if (nodeNetworkDataRecordHash != null) {
            if (nodeNetworkDataRecord.getNodeStatus() == NetworkNodeStatus.INACTIVE || nodeNetworkDataRecord.getStatusChainRef() == null) {
                return new ImmutablePair<>(eventDate, nodeNetworkDataRecordHash);
            } else {
                return nodeNetworkDataRecord.getStatusChainRef();
            }
        }

        LocalDate localDate = nodeDailyActivityData.getNodeDaySet().lower(eventDate);
        NodeHistoryData nodeHistoryData;
        Hash nodeHistoryDataHash;

        while (localDate != null) {
            nodeHistoryDataHash = networkHistoryService.calculateNodeHistoryDataHash(nodeDailyActivityData.getNodeHash(), localDate);
            nodeHistoryData = nodeHistory.getByHash(nodeHistoryDataHash);
            if (!nodeHistoryData.getNodeNetworkDataRecordMap().isEmpty()) {
                nodeNetworkDataRecordHash = nodeHistoryData.getNodeNetworkDataRecordMap().lastKey();
                nodeNetworkDataRecord = nodeHistoryData.getNodeNetworkDataRecordMap().get(nodeNetworkDataRecordHash);

                if (nodeNetworkDataRecord.getNodeStatus() == NetworkNodeStatus.INACTIVE || nodeNetworkDataRecord.getStatusChainRef() == null) {
                    return new ImmutablePair<>(localDate, nodeNetworkDataRecordHash);
                } else {
                    return nodeNetworkDataRecord.getStatusChainRef();
                }
            }
            localDate = nodeDailyActivityData.getNodeDaySet().lower(localDate);
        }
        return null;
    }

    private LinkedMap<Hash, NodeNetworkDataRecord> reCreateLinkedMapNodeNetworkDataRecord(LinkedMap<Hash, NodeNetworkDataRecord> oldNodeNetworkDataRecordMap,
                                                                                          NodeNetworkDataRecord newNodeNetworkDataRecord) {
        LinkedMap<Hash, NodeNetworkDataRecord> newNodeNetworkDataRecordMap = new LinkedMap<>();
        boolean isAlreadyInsertedFlag = false;
        if (!oldNodeNetworkDataRecordMap.isEmpty()) {
            Hash nodeNetworkDataRecordHash = oldNodeNetworkDataRecordMap.firstKey();
            while (nodeNetworkDataRecordHash != null) {
                NodeNetworkDataRecord nodeNetworkDataRecord = oldNodeNetworkDataRecordMap.get(nodeNetworkDataRecordHash);
                if (!isAlreadyInsertedFlag && nodeNetworkDataRecord.getRecordTime().equals(newNodeNetworkDataRecord.getRecordTime())
                        && newNodeNetworkDataRecord.getNodeStatus() == NetworkNodeStatus.INACTIVE) {
                    newNodeNetworkDataRecordMap.put(newNodeNetworkDataRecord.getHash(), newNodeNetworkDataRecord);
                    isAlreadyInsertedFlag = true;
                }
                if (!isAlreadyInsertedFlag && nodeNetworkDataRecord.getRecordTime().isAfter(newNodeNetworkDataRecord.getRecordTime())) {
                    newNodeNetworkDataRecordMap.put(newNodeNetworkDataRecord.getHash(), newNodeNetworkDataRecord);
                    isAlreadyInsertedFlag = true;
                }
                newNodeNetworkDataRecordMap.put(nodeNetworkDataRecordHash, nodeNetworkDataRecord);
                if (!isAlreadyInsertedFlag && nodeNetworkDataRecord.getRecordTime().equals(newNodeNetworkDataRecord.getRecordTime())
                        && newNodeNetworkDataRecord.getNodeStatus() != NetworkNodeStatus.INACTIVE) {
                    newNodeNetworkDataRecordMap.put(newNodeNetworkDataRecord.getHash(), newNodeNetworkDataRecord);
                    isAlreadyInsertedFlag = true;
                }
                nodeNetworkDataRecordHash = oldNodeNetworkDataRecordMap.nextKey(nodeNetworkDataRecordHash);
            }
        }
        if (!isAlreadyInsertedFlag) {
            newNodeNetworkDataRecordMap.put(newNodeNetworkDataRecord.getHash(), newNodeNetworkDataRecord);
        }
        return newNodeNetworkDataRecordMap;
    }

    private Pair<LocalDate, Hash> updateNodeHistoryDataForDateForACTIVE(NodeHistoryData nodeHistoryData, NodeNetworkDataRecord newNodeNetworkDataRecord,
                                                                        Pair<LocalDate, Hash> reference, boolean isCurrentDate) {
        Hash nodeNetworkDataRecordHash;
        if (isCurrentDate) {
            nodeNetworkDataRecordHash = nodeHistoryData.getNodeNetworkDataRecordMap().nextKey(newNodeNetworkDataRecord.getHash());
        } else {
            nodeNetworkDataRecordHash = nodeHistoryData.getNodeNetworkDataRecordMap().firstKey();
        }

        while (nodeNetworkDataRecordHash != null) {
            NodeNetworkDataRecord nodeNetworkDataRecord = nodeHistoryData.getNodeNetworkDataRecordMap().get(nodeNetworkDataRecordHash);
            nodeNetworkDataRecord.setStatusChainRef(reference);
            if (nodeNetworkDataRecord.getNodeStatus() == NetworkNodeStatus.INACTIVE) {
                return null;
            }
            nodeNetworkDataRecordHash = nodeHistoryData.getNodeNetworkDataRecordMap().nextKey(nodeNetworkDataRecordHash);
        }
        return reference;
    }

    private Pair<LocalDate, Hash> updateNodeHistoryDataForDateForINACTIVE(NodeHistoryData nodeHistoryData, NodeNetworkDataRecord newNodeNetworkDataRecord,
                                                                          Pair<LocalDate, Hash> reference, boolean isCurrentDate, LocalDate currentDate) {
        Hash nodeNetworkDataRecordHash;
        if (isCurrentDate) {
            nodeNetworkDataRecordHash = nodeHistoryData.getNodeNetworkDataRecordMap().nextKey(newNodeNetworkDataRecord.getHash());
        } else {
            nodeNetworkDataRecordHash = nodeHistoryData.getNodeNetworkDataRecordMap().firstKey();
        }
        if (nodeNetworkDataRecordHash == null) {
            return reference;
        }

        NodeNetworkDataRecord nodeNetworkDataRecord = nodeHistoryData.getNodeNetworkDataRecordMap().get(nodeNetworkDataRecordHash);
        nodeNetworkDataRecord.setStatusChainRef(reference);
        if (nodeNetworkDataRecord.getNodeStatus() == NetworkNodeStatus.INACTIVE) {
            return null;
        }
        return updateNodeHistoryDataForDateForACTIVE(nodeHistoryData, nodeNetworkDataRecord, new ImmutablePair<>(currentDate, nodeNetworkDataRecordHash), true);
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