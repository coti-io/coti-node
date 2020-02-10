package io.coti.nodemanager.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.exceptions.NetworkNodeValidationException;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.nodemanager.data.*;
import io.coti.nodemanager.exceptions.NetworkNodeRecordValidationException;
import io.coti.nodemanager.http.AddNodePairEventRequest;
import io.coti.nodemanager.http.AddNodeSingleEventRequest;
import io.coti.nodemanager.http.data.SingleNodeDetailsForWallet;
import io.coti.nodemanager.model.ActiveNodes;
import io.coti.nodemanager.model.NodeDailyActivities;
import io.coti.nodemanager.model.NodeHistory;
import io.coti.nodemanager.model.ReservedHosts;
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
import static io.coti.basenode.http.BaseNodeHttpStringConstants.SERVER_ERROR;
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
                    throw new NetworkNodeValidationException(String.format(INVALID_NODE_SERVER_URL_HOST_RESERVED, webServerUrl, host));
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
                    if (!nodeStatus.equals(NetworkNodeStatus.ACTIVE)) {
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

        Pair<LocalDate, Hash> reference;

        NodeNetworkDataRecord lastNodeNetworkDataRecord = networkHistoryService.getLastNodeNetworkDataRecord(nodeHash, nodeDailyActivityData);
        if (lastNodeNetworkDataRecord == null) {
            return;
        }
        if (lastNodeNetworkDataRecord.getNodeStatus().equals(NetworkNodeStatus.ACTIVE)) {
            NodeNetworkDataRecord previousNodeNetworkDataRecord = networkHistoryService.getNodeNetworkDataRecordByChainRef(lastNodeNetworkDataRecord);
            if (previousNodeNetworkDataRecord == null || !previousNodeNetworkDataRecord.getNodeStatus().equals(NetworkNodeStatus.ACTIVE)) {
                reference = new ImmutablePair<>(lastNodeNetworkDataRecord.getRecordTime().atZone(ZoneId.of("UTC")).toLocalDate(), lastNodeNetworkDataRecord.getHash());
            } else {
                reference = lastNodeNetworkDataRecord.getStatusChainRef();
            }
        } else {
            reference = networkHistoryService.getReferenceToRecord(lastNodeNetworkDataRecord);
        }
        nodeNetworkDataRecord.setStatusChainRef(reference);
    }

    @Override
    public ResponseEntity<IResponse> addSingleNodeEvent(AddNodeSingleEventRequest request) {
        Hash nodeHash = request.getNodeHash();
        NodeType nodeType = request.getNodeType();
        NetworkNodeStatus nodeStatus = request.getNodeStatus();
        Instant recordTime = request.getRecordTime();
        LocalDate localDateForEvent = recordTime.atZone(ZoneId.of("UTC")).toLocalDate();

        try {
            synchronized (addLockToLockMap(nodeHash)) {
                Instant nowInstant = Instant.now();
                NodeNetworkDataRecord lastNodeNetworkDataRecord = networkHistoryService.getLastNodeNetworkDataRecord(request.getNodeHash());

                validateSingleEventAddRequest(request, nowInstant, lastNodeNetworkDataRecord);

                NodeNetworkDataRecord newNodeNetworkDataRecord = createManualNodeNetworkDataRecord(nodeHash, nodeType, nodeStatus, recordTime);
                newNodeNetworkDataRecord.setStatusChainRef(networkHistoryService.getReferenceToRecord(lastNodeNetworkDataRecord));

                NodeDailyActivityData nodeDailyActivityData = nodeDailyActivities.getByHash(nodeHash);
                if (nodeDailyActivityData.getNodeDaySet().add(localDateForEvent)) {
                    nodeDailyActivities.put(nodeDailyActivityData);
                }

                NodeHistoryData nodeHistoryDataForEvent = getOrCreateNodeHistoryData(nodeHash, localDateForEvent, nodeType);
                nodeHistoryDataForEvent.getNodeNetworkDataRecordMap().put(newNodeNetworkDataRecord.getHash(), newNodeNetworkDataRecord);
                nodeHistory.put(nodeHistoryDataForEvent);

                return ResponseEntity.status(HttpStatus.OK).
                        body(new Response(String.format(ADDING_SINGLE_EVENT_ADDED_MANUALLY, nodeHash)));
            }
        } catch (NetworkNodeRecordValidationException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(e.getMessage(), SERVER_ERROR));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(e.getMessage(), SERVER_ERROR));
        } finally {
            removeLockFromLocksMap(nodeHash);
        }
    }

    @Override
    public ResponseEntity<IResponse> addPairNodeEvent(AddNodePairEventRequest request) {
        Hash nodeHash = request.getNodeHash();
        NodeType nodeType = request.getNodeType();
        NetworkNodeStatus firstEventNodeStatus = request.getFirstEventNodeStatus();
        Instant pairRequestStartTime = request.getStartTime();
        Instant pairRequestEndTime = request.getEndTime();

        try {
            synchronized (addLockToLockMap(nodeHash)) {
                Instant nowInstant = Instant.now();
                NodeNetworkDataRecord networkRecordBeforePair = getPreviousNetworkRecord(request.getNodeHash(), pairRequestStartTime);
                NodeNetworkDataRecord networkRecordAfterPair;
                if (pairRequestEndTime != null) {
                    networkRecordAfterPair = getNextNetworkRecord(request.getNodeHash(), pairRequestEndTime);
                } else {
                    networkRecordAfterPair = getNextNetworkRecord(request.getNodeHash(), pairRequestStartTime);
                    if (networkRecordAfterPair != null) {
                        pairRequestEndTime = networkRecordAfterPair.getRecordTime();
                    }
                }
                validatePairEventAddRequest(request, nowInstant, networkRecordBeforePair, networkRecordAfterPair);

                NodeNetworkDataRecord newPairStartNodeNetworkDataRecord = addNodeNetworkDataRecordForPairNodeEvent(nodeHash, nodeType, pairRequestStartTime, networkRecordBeforePair, firstEventNodeStatus, false);

                NetworkNodeStatus secondEventNodeStatus = firstEventNodeStatus == NetworkNodeStatus.ACTIVE ? NetworkNodeStatus.INACTIVE : NetworkNodeStatus.ACTIVE;
                NodeNetworkDataRecord newPairEndNodeNetworkDataRecord = addNodeNetworkDataRecordForPairNodeEvent(nodeHash, nodeType, pairRequestEndTime, newPairStartNodeNetworkDataRecord, secondEventNodeStatus, true);

                if (networkRecordAfterPair != null) {
                    NodeHistoryData nodeHistoryDataForEventAfterPair = getOrCreateNodeHistoryData(nodeHash, networkRecordAfterPair.getRecordTime().atZone(ZoneId.of("UTC")).toLocalDate(), nodeType);
                    nodeHistoryDataForEventAfterPair.getNodeNetworkDataRecordMap().get(networkRecordAfterPair.getHash()).setStatusChainRef(networkHistoryService.getReferenceToRecord(newPairEndNodeNetworkDataRecord));
                    nodeHistory.put(nodeHistoryDataForEventAfterPair);
                }

                return ResponseEntity.status(HttpStatus.OK).
                        body(new Response(String.format(ADDING_PAIR_EVENTS_ADDED_MANUALLY, nodeHash)));
            }
        } catch (NetworkNodeRecordValidationException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new Response(e.getMessage(), SERVER_ERROR));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(e.getMessage(), SERVER_ERROR));
        } finally {
            removeLockFromLocksMap(nodeHash);
        }
    }

    private NodeNetworkDataRecord addNodeNetworkDataRecordForPairNodeEvent(Hash nodeHash, NodeType nodeType, Instant eventRecordTime,
                                                                           NodeNetworkDataRecord previousNodeNetworkDataRecord, NetworkNodeStatus eventNodeStatus, boolean asFirst) {
        LocalDate localDate = eventRecordTime.atZone(ZoneId.of("UTC")).toLocalDate();
        NodeNetworkDataRecord newNodeNetworkDataRecord = createManualNodeNetworkDataRecord(nodeHash, nodeType, eventNodeStatus, eventRecordTime);
        if (previousNodeNetworkDataRecord != null) {
            newNodeNetworkDataRecord.setStatusChainRef(networkHistoryService.getReferenceToRecord(previousNodeNetworkDataRecord));
        }
        NodeHistoryData nodeHistoryData = getOrCreateNodeHistoryData(nodeHash, localDate, nodeType);
        nodeHistoryData.setNodeNetworkDataRecordMap(reCreateLinkedMapNodeNetworkDataRecord(nodeHistoryData.getNodeNetworkDataRecordMap(), newNodeNetworkDataRecord, asFirst));
        nodeHistory.put(nodeHistoryData);
        return newNodeNetworkDataRecord;
    }

    private LinkedMap<Hash, NodeNetworkDataRecord> reCreateLinkedMapNodeNetworkDataRecord(LinkedMap<Hash, NodeNetworkDataRecord> oldNodeNetworkDataRecordMap,
                                                                                          NodeNetworkDataRecord newNodeNetworkDataRecord, boolean asFirst) {
        LinkedMap<Hash, NodeNetworkDataRecord> newNodeNetworkDataRecordMap = new LinkedMap<>();
        Instant newNodeNetworkDataRecordTime = newNodeNetworkDataRecord.getRecordTime();
        Hash newNodeNetworkDataRecordHash = newNodeNetworkDataRecord.getHash();
        boolean isAlreadyInsertedFlag = false;
        if (!oldNodeNetworkDataRecordMap.isEmpty()) {
            Hash nodeNetworkDataRecordHash = oldNodeNetworkDataRecordMap.firstKey();
            while (nodeNetworkDataRecordHash != null) {
                NodeNetworkDataRecord nodeNetworkDataRecord = oldNodeNetworkDataRecordMap.get(nodeNetworkDataRecordHash);
                if (isAlreadyInsertedFlag) {
                    newNodeNetworkDataRecordMap.put(nodeNetworkDataRecordHash, nodeNetworkDataRecord);
                } else {
                    if (nodeNetworkDataRecord.getRecordTime().isBefore(newNodeNetworkDataRecordTime)
                            || (nodeNetworkDataRecord.getRecordTime().equals(newNodeNetworkDataRecordTime) && !asFirst)) {
                        newNodeNetworkDataRecordMap.put(nodeNetworkDataRecordHash, nodeNetworkDataRecord);
                    } else {
                        if ((nodeNetworkDataRecord.getRecordTime().equals(newNodeNetworkDataRecordTime) && asFirst)
                                || nodeNetworkDataRecord.getRecordTime().isAfter(newNodeNetworkDataRecordTime)) {
                            newNodeNetworkDataRecordMap.put(newNodeNetworkDataRecordHash, newNodeNetworkDataRecord);
                            isAlreadyInsertedFlag = true;
                            newNodeNetworkDataRecordMap.put(nodeNetworkDataRecordHash, nodeNetworkDataRecord);
                        }
                    }
                }
                nodeNetworkDataRecordHash = oldNodeNetworkDataRecordMap.nextKey(nodeNetworkDataRecordHash);
            }
        }
        if (!isAlreadyInsertedFlag) {
            newNodeNetworkDataRecordMap.put(newNodeNetworkDataRecordHash, newNodeNetworkDataRecord);
        }
        return newNodeNetworkDataRecordMap;
    }


    private void validatePairEventAddRequest(AddNodePairEventRequest request, Instant nowInstant,
                                             NodeNetworkDataRecord networkRecordBeforePair, NodeNetworkDataRecord networkRecordAfterPair) {
        if (request.getNodeType() != NodeType.FullNode) {
            throw new NetworkNodeRecordValidationException(String.format(ADDING_PAIR_EVENTS_INCORRECT_NODE_TYPE, request.getNodeHash()));
        }
        NetworkNodeStatus firstEventNodeStatus = request.getFirstEventNodeStatus();
        NetworkNodeStatus secondEventNodeStatus = request.getFirstEventNodeStatus() == NetworkNodeStatus.ACTIVE ? NetworkNodeStatus.INACTIVE : NetworkNodeStatus.ACTIVE;
        if (networkRecordBeforePair != null && networkRecordBeforePair.getNodeStatus() == firstEventNodeStatus ||
                networkRecordAfterPair != null && networkRecordAfterPair.getNodeStatus() == secondEventNodeStatus
        ) {
            throw new NetworkNodeRecordValidationException(String.format(ADDING_PAIR_EVENTS_CONSECUTIVE_STATUS, request.getNodeHash()));
        }
        if (request.getEndTime() != null && request.getEndTime().isAfter(nowInstant)) {
            throw new NetworkNodeRecordValidationException(String.format(ADDING_PAIR_EVENTS_INCORRECT_FUTURE_TIME, request.getNodeHash()));
        }
        if (request.getEndTime() == null && request.getStartTime().isAfter(nowInstant)) {
            throw new NetworkNodeRecordValidationException(String.format(ADDING_PAIR_EVENTS_START_IN_FUTURE, request.getNodeHash()));
        }
        if (request.getEndTime() == null && networkRecordAfterPair == null) {
            throw new NetworkNodeRecordValidationException(String.format(ADDING_PAIR_EVENTS_AFTER_LAST_EVENT_OPEN_ENDED, request.getNodeHash()));
        }
        if (request.getEndTime() != null && checkNoEventsWithinTimeRange(request.getNodeHash(), request.getStartTime(), request.getEndTime())) {
            throw new NetworkNodeRecordValidationException(String.format(ADDING_PAIR_EVENTS_WITH_EXISTING_EVENTS_INSIDE, request.getNodeHash()));
        }
    }

    private boolean checkNoEventsWithinTimeRange(Hash nodeHash, Instant startTime, Instant endTime) {
        NodeNetworkDataRecord networkRecordAfterStartTime = getNextNetworkRecord(nodeHash, startTime);
        if (networkRecordAfterStartTime != null) {
            return networkRecordAfterStartTime.getRecordTime().isBefore(endTime);
        }
        return false;
    }

    private NodeNetworkDataRecord getNextNetworkRecord(Hash nodeHash, Instant instant) {
        NodeNetworkDataRecord nextNodeNetworkDataRecord = null;
        if (nodeHash != null && instant != null) {
            LocalDate localDate = instant.atZone(ZoneId.of("UTC")).toLocalDate();
            NodeDailyActivityData nodeDailyActivityData = nodeDailyActivities.getByHash(nodeHash);
            if (nodeDailyActivityData != null) {
                ConcurrentSkipListSet<LocalDate> nodeDaySet = nodeDailyActivityData.getNodeDaySet();
                LocalDate ceilingLocalDateForNewEvent = nodeDaySet.ceiling(localDate);
                if (ceilingLocalDateForNewEvent != null) {
                    NodeHistoryData nodeHistoryData = networkHistoryService.getNodeHistoryData(nodeHash, ceilingLocalDateForNewEvent);
                    if (ceilingLocalDateForNewEvent.equals(localDate)) {
                        nextNodeNetworkDataRecord = getNextNetworkRecord(nodeHash, instant, nodeHistoryData, ceilingLocalDateForNewEvent, nodeDaySet);
                    } else {
                        nextNodeNetworkDataRecord = networkHistoryService.getFirstNodeNetworkDataRecord(nodeHistoryData);
                    }
                }
            }
        }
        return nextNodeNetworkDataRecord;
    }

    private NodeNetworkDataRecord getNextNetworkRecord(Hash nodeHash, Instant instant,
                                                       NodeHistoryData nodeHistoryDataForEvent, LocalDate localDate, ConcurrentSkipListSet<LocalDate> nodeDaySet) {
        LinkedMap<Hash, NodeNetworkDataRecord> nodeNetworkDataRecordMap = nodeHistoryDataForEvent.getNodeNetworkDataRecordMap();
        NodeNetworkDataRecord nextNodeNetworkDataRecord = null;
        NodeNetworkDataRecord lastInDayNodeNetworkDataRecord = nodeNetworkDataRecordMap.get(nodeNetworkDataRecordMap.lastKey());

        if (!lastInDayNodeNetworkDataRecord.getRecordTime().isBefore(instant)) {
            for (Map.Entry<Hash, NodeNetworkDataRecord> entry : nodeNetworkDataRecordMap.entrySet()) {
                if (!entry.getValue().getRecordTime().isBefore(instant)) {
                    nextNodeNetworkDataRecord = entry.getValue();
                    break;
                }
            }
        } else {
            LocalDate higherLocalDate = nodeDaySet.higher(localDate);
            if (higherLocalDate != null) {
                NodeHistoryData nodeHistoryDataForEventFromHigherDate = networkHistoryService.getNodeHistoryData(nodeHash, higherLocalDate);
                nextNodeNetworkDataRecord = networkHistoryService.getFirstNodeNetworkDataRecord(nodeHistoryDataForEventFromHigherDate);
            }
        }
        return nextNodeNetworkDataRecord;
    }

    private NodeNetworkDataRecord getPreviousNetworkRecord(Hash nodeHash, Instant instant) {
        NodeNetworkDataRecord previousNodeNetworkDataRecord = null;
        if (nodeHash != null && instant != null) {
            LocalDate localDate = instant.atZone(ZoneId.of("UTC")).toLocalDate();
            NodeDailyActivityData nodeDailyActivityData = nodeDailyActivities.getByHash(nodeHash);
            if (nodeDailyActivityData != null) {
                ConcurrentSkipListSet<LocalDate> nodeDaySet = nodeDailyActivityData.getNodeDaySet();
                LocalDate floorLocalDate = nodeDaySet.floor(localDate);
                if (floorLocalDate != null) {
                    NodeHistoryData nodeHistoryDataForEvent = networkHistoryService.getNodeHistoryData(nodeHash, floorLocalDate);
                    if (floorLocalDate.equals(localDate)) {
                        previousNodeNetworkDataRecord = getPreviousNetworkRecord(nodeHash, instant, nodeHistoryDataForEvent, floorLocalDate, nodeDaySet);
                    } else {
                        previousNodeNetworkDataRecord = networkHistoryService.getLastNodeNetworkDataRecord(nodeHistoryDataForEvent);
                    }
                }
            }
        }
        return previousNodeNetworkDataRecord;
    }

    private NodeNetworkDataRecord getPreviousNetworkRecord(Hash nodeHash, Instant instant,
                                                           NodeHistoryData nodeHistoryData, LocalDate localDate, ConcurrentSkipListSet<LocalDate> nodeDaySet) {
        LinkedMap<Hash, NodeNetworkDataRecord> nodeNetworkDataRecordMap = nodeHistoryData.getNodeNetworkDataRecordMap();
        NodeNetworkDataRecord firstInDayNodeNetworkDataRecord = nodeNetworkDataRecordMap.get(nodeNetworkDataRecordMap.firstKey());
        NodeNetworkDataRecord previousNodeNetworkDataRecord = null;

        if (!firstInDayNodeNetworkDataRecord.getRecordTime().isAfter(instant)) {
            for (Map.Entry<Hash, NodeNetworkDataRecord> entry : nodeNetworkDataRecordMap.entrySet()) {
                if (entry.getValue().getRecordTime().isAfter(instant)) {
                    break;
                }
                previousNodeNetworkDataRecord = entry.getValue();
            }
        } else {
            LocalDate lowerLocalDate = nodeDaySet.lower(localDate);
            if (lowerLocalDate != null) {
                NodeHistoryData nodeHistoryDataForEventFromHigherDate = networkHistoryService.getNodeHistoryData(nodeHash, lowerLocalDate);
                previousNodeNetworkDataRecord = networkHistoryService.getLastNodeNetworkDataRecord(nodeHistoryDataForEventFromHigherDate);
            }
        }
        return previousNodeNetworkDataRecord;
    }

    private void validateSingleEventAddRequest(AddNodeSingleEventRequest request, Instant nowInstant, NodeNetworkDataRecord lastNodeNetworkDataRecord) {
        if (request.getNodeType() != NodeType.FullNode) {
            throw new NetworkNodeRecordValidationException(String.format(ADDING_SINGLE_EVENT_INCORRECT_NODE_TYPE, request.getNodeHash()));
        }
        if (request.getNodeStatus() != NetworkNodeStatus.INACTIVE) {
            throw new NetworkNodeRecordValidationException(String.format(ADDING_SINGLE_EVENT_INCORRECT_NETWORK_NODE_STATUS, request.getNodeHash()));
        }
        if (lastNodeNetworkDataRecord == null) {
            throw new NetworkNodeRecordValidationException(String.format(ADDING_SINGLE_EVENT_NON_ACTIVATED_NODE, request.getNodeHash()));
        }
        if (lastNodeNetworkDataRecord.getNodeStatus() != NetworkNodeStatus.ACTIVE) {
            throw new NetworkNodeRecordValidationException(String.format(ADDING_SINGLE_EVENT_INCORRECT_LAST_STATUS_NOT_ACTIVE, request.getNodeHash()));
        }
        Instant lastExistingRecordTime = lastNodeNetworkDataRecord.getRecordTime();
        if (lastExistingRecordTime.isAfter(request.getRecordTime())) {
            throw new NetworkNodeRecordValidationException(String.format(ADDING_SINGLE_EVENT_INCORRECT_TIME_BEFORE_LAST_EXISTING_STATUS, request.getNodeHash()));
        }
        if (request.getRecordTime().isAfter(nowInstant)) {
            throw new NetworkNodeRecordValidationException(String.format(ADDING_SINGLE_EVENT_INCORRECT_FUTURE_TIME, request.getNodeHash()));
        }
    }

    private NodeNetworkDataRecord createManualNodeNetworkDataRecord(Hash nodeHash, NodeType nodeType, NetworkNodeStatus nodeStatus, Instant recordTime) {
        NetworkNodeData networkNodeData = new NetworkNodeData();
        networkNodeData.setHash(nodeHash);
        networkNodeData.setNodeType(nodeType);
        NodeNetworkDataRecord newNodeNetworkDataRecord = new NodeNetworkDataRecord(recordTime, nodeStatus, networkNodeData);
        newNodeNetworkDataRecord.setNotOriginalEvent(true);
        return newNodeNetworkDataRecord;
    }

    private NodeHistoryData getOrCreateNodeHistoryData(Hash nodeHash, LocalDate currentEventDate, NodeType nodeType) {
        Hash nodeHistoryDataHash = networkHistoryService.calculateNodeHistoryDataHash(nodeHash, currentEventDate);
        NodeHistoryData nodeHistoryData = nodeHistory.getByHash(nodeHistoryDataHash);
        if (nodeHistoryData == null) {
            NodeDailyActivityData nodeDailyActivityData = nodeDailyActivities.getByHash(nodeHash);
            if (nodeDailyActivityData == null) {
                nodeDailyActivityData = new NodeDailyActivityData(nodeHash, nodeType);
            }
            ConcurrentSkipListSet<LocalDate> nodeDaySet = nodeDailyActivityData.getNodeDaySet();
            nodeDaySet.add(currentEventDate);
            nodeDailyActivities.put(nodeDailyActivityData);
            nodeHistoryData = new NodeHistoryData(nodeHistoryDataHash);
        }
        return nodeHistoryData;
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