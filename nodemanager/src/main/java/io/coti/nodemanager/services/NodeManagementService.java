package io.coti.nodemanager.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.exceptions.NetworkNodeValidationException;
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
import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.INVALID_NODE_SERVER_URL_HOST_RESERVED;
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
                    if (!nodeStatus.equals(NetworkNodeStatus.ACTIVE)) {
                        return;
                    }
                    nodeDailyActivityData = new NodeDailyActivityData(nodeHash, networkNodeData.getNodeType());
                }
                NodeNetworkDataRecord newNodeNetworkDataRecord =
                        new NodeNetworkDataRecord(currentEventDateTime, nodeStatus, networkNodeData);
                addReferenceToNodeNetworkDataRecord(nodeDailyActivityData, nodeHash, newNodeNetworkDataRecord);
                NodeHistoryData nodeHistoryData = getOrCreateNodeHistoryData(nodeHash, currentEventDate);
                nodeHistoryData.getNodeNetworkDataRecordMap().put(newNodeNetworkDataRecord.getHash(), newNodeNetworkDataRecord);
                nodeHistory.put(nodeHistoryData);
                nodeDailyActivityData.getNodeDaySet().add(currentEventDate);
                nodeDailyActivities.put(nodeDailyActivityData);
            }
        } finally {
            removeLockFromLocksMap(nodeHash);
        }
    }

    private NodeHistoryData getOrCreateNodeHistoryData(Hash nodeHash, LocalDate currentEventDate) {
        Hash nodeHistoryDataHash = networkHistoryService.calculateNodeHistoryDataHash(nodeHash, currentEventDate);
        NodeHistoryData nodeHistoryData = nodeHistory.getByHash(nodeHistoryDataHash);
        if (nodeHistoryData == null) {
            nodeHistoryData = new NodeHistoryData(nodeHistoryDataHash);
        }
        return nodeHistoryData;
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
        Hash nodeHash = request.getNodeHash();
        Instant dateTimeForSecondEvent = findVeryFirstActiveEvent(nodeHash);

        if (dateTimeForSecondEvent == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                    body(new Response(String.format(ADDING_EVENT_PAIR_FAILED, nodeHash)));
        }
        Instant dateTimeEvent = request.getStartDateTimeUTC();
        if (!dateTimeForSecondEvent.isAfter(dateTimeEvent)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                    body(new Response(String.format(ADDING_EVENT_PAIR_FAILED, nodeHash)));
        }

        addNodeEventAdminRequest = new AddNodeEventAdminRequest(nodeHash, request.getStartDateTimeUTC(), NodeTypeName.FullNode.getNode(), NetworkNodeStatus.ACTIVE.toString());
        ResponseEntity<IResponse> response1 = addNodeEventAdmin(addNodeEventAdminRequest, true);
        if (response1.getStatusCode() != HttpStatus.OK) {
            return response1;
        }

        addNodeEventAdminRequest = new AddNodeEventAdminRequest(nodeHash, dateTimeForSecondEvent, NodeTypeName.FullNode.getNode(), NetworkNodeStatus.INACTIVE.toString());
        return addNodeEventAdmin(addNodeEventAdminRequest, true);
    }

    private Instant findVeryFirstActiveEvent(Hash nodeHash) {
        NodeDailyActivityData nodeDailyActivityData = nodeDailyActivities.getByHash(nodeHash);
        if (nodeDailyActivityData == null) {
            return null;
        }

        LocalDate localDate = nodeDailyActivityData.getNodeDaySet().first();

        NodeHistoryData nodeHistoryData = null;
        while (nodeHistoryData == null) {
            Hash nodeHistoryDataHash = networkHistoryService.calculateNodeHistoryDataHash(nodeHash, localDate);
            nodeHistoryData = nodeHistory.getByHash(nodeHistoryDataHash);

            if (nodeHistoryData != null && !nodeHistoryData.getNodeNetworkDataRecordMap().isEmpty()) {
                Hash nodeNetworkDataRecordHash = nodeHistoryData.getNodeNetworkDataRecordMap().firstKey();
                NodeNetworkDataRecord nodeNetworkDataRecord = nodeHistoryData.getNodeNetworkDataRecordMap().get(nodeNetworkDataRecordHash);
                if (nodeNetworkDataRecord.getNodeStatus() != NetworkNodeStatus.ACTIVE) {
                    return null;
                } else {
                    return nodeNetworkDataRecord.getRecordTime();
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
    public ResponseEntity<IResponse> addNodeEventSingleAdmin(AddNodeEventAdminRequest request) {
        return addNodeEventAdmin(request, false);
    }

    private ResponseEntity<IResponse> addNodeEventAdmin(AddNodeEventAdminRequest request, boolean isPair) {

        Hash nodeHash = request.getNodeHash();
        Instant instantDateTimeEvent = request.getRecordDateTimeUTC();
        LocalDate localDateForEvent = LocalDateTime.ofInstant(instantDateTimeEvent, ZoneOffset.UTC).toLocalDate();

        Instant nowInstant = Instant.now().atZone(ZoneId.of("UTC")).toInstant();
        ResponseEntity<IResponse> validationResponse = validateAddNodeEventRequest(request, isPair, nodeHash, instantDateTimeEvent, localDateForEvent, nowInstant);
        if (validationResponse != null) return validationResponse;

        NetworkNodeData networkNodeData = new NetworkNodeData();
        networkNodeData.setHash(nodeHash);
        networkNodeData.setNodeType(NodeTypeName.getNodeType(request.getNodeType()));

        NetworkNodeStatus nodeStatusForEvent = NetworkNodeStatus.enumFromString(request.getNodeStatus());
        NodeNetworkDataRecord newNodeNetworkDataRecord =
                new NodeNetworkDataRecord(instantDateTimeEvent, nodeStatusForEvent, networkNodeData);
        newNodeNetworkDataRecord.setNotOriginalEvent(true);

        try {
            synchronized (addLockToLockMap(nodeHash)) {
                NodeDailyActivityData nodeDailyActivityData = nodeDailyActivities.getByHash(nodeHash);
                if (nodeDailyActivityData == null) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                            body(new Response(String.format(ADDING_EVENT_NONACTIVATED_NODE_HASH, nodeHash)));
                }
                if (nodeDailyActivityData.getNodeDaySet().add(localDateForEvent)) {
                    nodeDailyActivities.put(nodeDailyActivityData);
                }

                NodeHistoryData nodeHistoryDataForEvent = getOrCreateNodeHistoryData(nodeHash, localDateForEvent);
                nodeHistoryDataForEvent.setNodeNetworkDataRecordMap(reCreateLinkedMapNodeNetworkDataRecord(nodeHistoryDataForEvent.getNodeNetworkDataRecordMap(), newNodeNetworkDataRecord));

                Pair<LocalDate, Hash> reference = findReferenceToPreviousEvent(nodeDailyActivityData, nodeHistoryDataForEvent, newNodeNetworkDataRecord, localDateForEvent);
                newNodeNetworkDataRecord.setStatusChainRef(reference);
                nodeHistory.put(nodeHistoryDataForEvent);

                updateNodeHistoryDataForDate(nodeHistoryDataForEvent, newNodeNetworkDataRecord, localDateForEvent);

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

    private void updateNodeHistoryDataForDate(NodeHistoryData nodeHistoryData, NodeNetworkDataRecord newNodeNetworkDataRecord,
                                              LocalDate localDateForEvent) {
        boolean flippedStatus = false;
        NodeHistoryData localNodeHistoryData = nodeHistoryData;
        NetworkNodeStatus newNodeNetworkDataRecordNodeStatus = newNodeNetworkDataRecord.getNodeStatus();
        Hash nodeNetworkDataRecordHash;
        nodeNetworkDataRecordHash = nodeHistoryData.getNodeNetworkDataRecordMap().nextKey(newNodeNetworkDataRecord.getHash());

        Hash nodeHash = newNodeNetworkDataRecord.getNetworkNodeData().getNodeHash();
        NodeDailyActivityData nodeDailyActivityData = nodeDailyActivities.getByHash(nodeHash);

        localNodeHistoryData = getNextDateNodeHistoryData(localDateForEvent, localNodeHistoryData, nodeNetworkDataRecordHash, nodeHash, nodeDailyActivityData);
        if (localNodeHistoryData != null && nodeNetworkDataRecordHash == null) {
            nodeNetworkDataRecordHash = localNodeHistoryData.getNodeNetworkDataRecordMap().firstKey();
        }
        Pair<LocalDate, Hash> eventDateHashPair = getLocalDateHashPair(newNodeNetworkDataRecord);

        NodeNetworkDataRecord nodeNetworkDataRecord = null;
        NodeNetworkDataRecord previousNodeNetworkDataRecord = null;
        boolean isFirstNewStatus = true;
        while (nodeNetworkDataRecordHash != null) {
            previousNodeNetworkDataRecord = nodeNetworkDataRecord != null ? nodeNetworkDataRecord : null;
            nodeNetworkDataRecord = localNodeHistoryData.getNodeNetworkDataRecordMap().get(nodeNetworkDataRecordHash);
            if (!flippedStatus) {
                if (nodeNetworkDataRecord.getNodeStatus() != newNodeNetworkDataRecordNodeStatus) {
                    isFirstNewStatus = false;
                }
                if(!nodeNetworkDataRecord.equals(newNodeNetworkDataRecord)) {
                    nodeNetworkDataRecord.setStatusChainRef(eventDateHashPair);
                }
                flippedStatus = nodeNetworkDataRecord.getNodeStatus() != newNodeNetworkDataRecordNodeStatus;
            } else {
                if (isFirstNewStatus || nodeNetworkDataRecord.getNodeStatus() != newNodeNetworkDataRecordNodeStatus) {
                    nodeNetworkDataRecord.setStatusChainRef(eventDateHashPair);
                    isFirstNewStatus = false;
                }
                eventDateHashPair = getLocalDateHashPair(nodeNetworkDataRecord);
                if (nodeNetworkDataRecord.getNodeStatus() == newNodeNetworkDataRecordNodeStatus) {
                    nodeHistory.put(localNodeHistoryData);
                    NodeNetworkDataRecord nodeNetworkDataRecordByChainRef =
                            networkHistoryService.getNodeNetworkDataRecordByChainRef(networkHistoryService.getNodeNetworkDataRecordByChainRef(nodeNetworkDataRecord));
                    if (nodeNetworkDataRecordByChainRef == null
                            || !nodeNetworkDataRecordByChainRef.getHash().equals(newNodeNetworkDataRecord.getHash())) {
                        nodeNetworkDataRecord.setStatusChainRef(getLocalDateHashPair(previousNodeNetworkDataRecord));
                    }

                    nodeHistory.put(localNodeHistoryData);
                    return;
                }
            }
            nodeNetworkDataRecordHash = localNodeHistoryData.getNodeNetworkDataRecordMap().nextKey(nodeNetworkDataRecordHash);
            if (nodeNetworkDataRecordHash == null) {
                nodeHistory.put(localNodeHistoryData);
                LocalDate previousEventLocalDate = nodeNetworkDataRecord.getRecordTime().atZone(ZoneId.of("UTC")).toLocalDate();
                if (previousEventLocalDate != null && nodeDailyActivityData.getNodeDaySet().higher(previousEventLocalDate) != null) {
                    localNodeHistoryData = getNextDateNodeHistoryData(previousEventLocalDate, localNodeHistoryData, nodeNetworkDataRecordHash, nodeHash, nodeDailyActivityData);
                    if (localNodeHistoryData != null) {
                        nodeNetworkDataRecordHash = localNodeHistoryData.getNodeNetworkDataRecordMap().firstKey();
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }
        }
    }

    private NodeHistoryData getNextDateNodeHistoryData(LocalDate localDateForEvent, NodeHistoryData localNodeHistoryData,
                                                       Hash nodeNetworkDataRecordHash, Hash nodeHash, NodeDailyActivityData nodeDailyActivityData) {
        if (nodeNetworkDataRecordHash == null) {
            LocalDate localDate = nodeDailyActivityData.getNodeDaySet().higher(localDateForEvent);
            if (localDate != null) {
                Hash nodeHistoryDataHash = networkHistoryService.calculateNodeHistoryDataHash(nodeHash, localDate);
                localNodeHistoryData = nodeHistory.getByHash(nodeHistoryDataHash);
            }
        }
        return localNodeHistoryData;
    }

    private Pair<LocalDate, Hash> getLocalDateHashPair(NodeNetworkDataRecord newNodeNetworkDataRecord) {
        LocalDate eventLocalDate = newNodeNetworkDataRecord.getRecordTime().atZone(ZoneId.of("UTC")).toLocalDate();
        return new ImmutablePair<>(eventLocalDate, newNodeNetworkDataRecord.getHash());
    }

    private ResponseEntity<IResponse> validateAddNodeEventRequest(AddNodeEventAdminRequest request, boolean isPair, Hash nodeHash, Instant instantDateTimeEvent, LocalDate localDateForEvent, Instant nowInstant) {
        if (instantDateTimeEvent.isAfter(nowInstant)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                    body(new Response(String.format(ADDING_EVENT_INCORRECT_FUTURE_TIME, nodeHash)));
        }
        if (!isPair && NetworkNodeStatus.enumFromString(request.getNodeStatus()).equals(NetworkNodeStatus.INACTIVE)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                    body(new Response(String.format(ADDING_EVENT_INCORRECT_INACTIVE_STATUS, nodeHash)));
        }

        NetworkNodeStatus previousEventStatus = getNodeStatusOfPreviousActivityEvent(nodeHash, instantDateTimeEvent, localDateForEvent);

        if (previousEventStatus != null && NetworkNodeStatus.enumFromString(request.getNodeStatus()).equals(previousEventStatus)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                    body(new Response(String.format(ADDING_EVENT_CONSECUTIVE_SAME_STATUS, nodeHash)));
        }
        return null;
    }

    private NetworkNodeStatus getNodeStatusOfPreviousActivityEvent(Hash nodeHash, Instant instantDateTimeEvent, LocalDate localDateForEvent) {
        NetworkNodeStatus previousEventStatus = null;
        NodeDailyActivityData nodeDailyActivityDataForChecks = nodeDailyActivities.getByHash(nodeHash);
        if (nodeDailyActivityDataForChecks != null) {
            LocalDate floorLocalDateForNewEvent = nodeDailyActivityDataForChecks.getNodeDaySet().floor(localDateForEvent);
            if (floorLocalDateForNewEvent != null) {
                Hash nodeHistoryDataHashForEventForChecks = networkHistoryService.calculateNodeHistoryDataHash(nodeHash, floorLocalDateForNewEvent);
                NodeHistoryData nodeHistoryDataForEvent = nodeHistory.getByHash(nodeHistoryDataHashForEventForChecks);
                if (floorLocalDateForNewEvent.equals(localDateForEvent)) {
                    previousEventStatus = getNodeStatusOfPreviousActivityEventFromNodeHistoryData(nodeHash, instantDateTimeEvent, nodeHistoryDataForEvent);
                } else {
                    previousEventStatus = nodeHistoryDataForEvent.getNodeNetworkDataRecordMap().get(nodeHistoryDataForEvent.getNodeNetworkDataRecordMap().lastKey()).getNodeStatus();
                }
            }
        }
        return previousEventStatus;
    }

    private NetworkNodeStatus getNodeStatusOfPreviousActivityEventFromNodeHistoryData(Hash nodeHash, Instant instantDateTimeEvent, NodeHistoryData nodeHistoryDataForEvent) {
        NetworkNodeStatus previousEventStatus = null;
        LinkedMap<Hash, NodeNetworkDataRecord> nodeNetworkDataRecordMap = nodeHistoryDataForEvent.getNodeNetworkDataRecordMap();
        NodeNetworkDataRecord previousNodeNetworkDataRecord = nodeNetworkDataRecordMap.get(nodeNetworkDataRecordMap.firstKey());
        if (previousNodeNetworkDataRecord.getRecordTime().isAfter(instantDateTimeEvent)) {
            Pair<LocalDate, Hash> statusChainRef = previousNodeNetworkDataRecord.getStatusChainRef();
            if (statusChainRef != null) {
                Hash nodeHistoryDataHashForEventByRef = networkHistoryService.calculateNodeHistoryDataHash(nodeHash, statusChainRef.getLeft());
                NodeHistoryData nodeHistoryDataForEventByRef = nodeHistory.getByHash(nodeHistoryDataHashForEventByRef);
                NodeNetworkDataRecord nodeNetworkDataRecordByRef =
                        nodeHistoryDataForEventByRef.getNodeNetworkDataRecordMap().get(nodeHistoryDataForEventByRef.getNodeNetworkDataRecordMap().lastKey());
                previousEventStatus = nodeNetworkDataRecordByRef.getNodeStatus();
            }
        } else {
            for (Map.Entry<Hash, NodeNetworkDataRecord> entry : nodeNetworkDataRecordMap.entrySet()) {
                if (!entry.getValue().getRecordTime().isBefore(instantDateTimeEvent)) {
                    previousEventStatus = previousNodeNetworkDataRecord.getNodeStatus();
                    break;
                }
                previousNodeNetworkDataRecord = entry.getValue();
                previousEventStatus = previousNodeNetworkDataRecord.getNodeStatus();
            }
        }
        return previousEventStatus;
    }

    private Pair<LocalDate, Hash> findReferenceToPreviousEvent(NodeDailyActivityData nodeDailyActivityData, NodeHistoryData eventNodeHistoryData,
                                                               NodeNetworkDataRecord newNodeNetworkDataRecord, LocalDate eventDate) {
        Hash nodeNetworkDataRecordHash = eventNodeHistoryData.getNodeNetworkDataRecordMap().previousKey(newNodeNetworkDataRecord.getHash());
        NodeNetworkDataRecord nodeNetworkDataRecord = null;

        if (nodeNetworkDataRecordHash != null) {
            nodeNetworkDataRecord = eventNodeHistoryData.getNodeNetworkDataRecordMap().get(nodeNetworkDataRecordHash);
        }
        if (nodeNetworkDataRecordHash != null) {
            return getLocalDateHashPair(nodeDailyActivityData, eventNodeHistoryData, eventDate, nodeNetworkDataRecordHash, nodeNetworkDataRecord);
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

                return getLocalDateHashPair(nodeDailyActivityData, nodeHistoryData, localDate, nodeNetworkDataRecordHash, nodeNetworkDataRecord);
            }
            localDate = nodeDailyActivityData.getNodeDaySet().lower(localDate);
        }
        return null;
    }

    private Pair<LocalDate, Hash> getLocalDateHashPair(NodeDailyActivityData nodeDailyActivityData, NodeHistoryData eventNodeHistoryData, LocalDate eventDate, Hash nodeNetworkDataRecordHash, NodeNetworkDataRecord nodeNetworkDataRecord) {
        if (nodeNetworkDataRecord.getNodeStatus() == NetworkNodeStatus.INACTIVE || nodeNetworkDataRecord.getStatusChainRef() == null) {
            return new ImmutablePair<>(eventDate, nodeNetworkDataRecordHash);
        } else {
            if (checkIfNodeEventByRefIsActive(nodeDailyActivityData.getNodeHash(), eventDate, eventNodeHistoryData, nodeNetworkDataRecord.getStatusChainRef())) {
                return nodeNetworkDataRecord.getStatusChainRef();
            } else {
                return new ImmutablePair<>(eventDate, nodeNetworkDataRecordHash);
            }
        }
    }

    private boolean checkIfNodeEventByRefIsActive(Hash nodeHash, LocalDate localDate, NodeHistoryData nodeHistoryData, Pair<LocalDate, Hash> reference) {
        NodeHistoryData nodeHistoryDataByRef;
        if (localDate.isEqual(reference.getLeft())) {
            nodeHistoryDataByRef = nodeHistoryData;
        } else {
            Hash nodeHistoryDataHash = networkHistoryService.calculateNodeHistoryDataHash(nodeHash, reference.getLeft());
            nodeHistoryDataByRef = nodeHistory.getByHash(nodeHistoryDataHash);
        }
        return nodeHistoryDataByRef != null && !nodeHistoryDataByRef.getNodeNetworkDataRecordMap().isEmpty()
                && nodeHistoryDataByRef.getNodeNetworkDataRecordMap().get(reference.getRight()).getNodeStatus() == NetworkNodeStatus.ACTIVE;
    }

    private LinkedMap<Hash, NodeNetworkDataRecord> reCreateLinkedMapNodeNetworkDataRecord(LinkedMap<Hash, NodeNetworkDataRecord> oldNodeNetworkDataRecordMap,
                                                                                          NodeNetworkDataRecord newNodeNetworkDataRecord) {
        LinkedMap<Hash, NodeNetworkDataRecord> newNodeNetworkDataRecordMap = new LinkedMap<>();
        Instant newNodeNetworkDataRecordTime = newNodeNetworkDataRecord.getRecordTime();
        Hash newNodeNetworkDataRecordHash = newNodeNetworkDataRecord.getHash();
        NetworkNodeStatus newNodeNetworkDataRecordNodeStatus = newNodeNetworkDataRecord.getNodeStatus();
        boolean isAlreadyInsertedFlag = false;
        if (!oldNodeNetworkDataRecordMap.isEmpty()) {
            Hash nodeNetworkDataRecordHash = oldNodeNetworkDataRecordMap.firstKey();
            while (nodeNetworkDataRecordHash != null) {
                NodeNetworkDataRecord nodeNetworkDataRecord = oldNodeNetworkDataRecordMap.get(nodeNetworkDataRecordHash);
                if (!isAlreadyInsertedFlag && nodeNetworkDataRecord.getRecordTime().equals(newNodeNetworkDataRecordTime)
                        && newNodeNetworkDataRecordNodeStatus == NetworkNodeStatus.INACTIVE) {
                    newNodeNetworkDataRecordMap.put(newNodeNetworkDataRecordHash, newNodeNetworkDataRecord);
                    isAlreadyInsertedFlag = true;
                }
                if (!isAlreadyInsertedFlag && nodeNetworkDataRecord.getRecordTime().isAfter(newNodeNetworkDataRecordTime)) {
                    newNodeNetworkDataRecordMap.put(newNodeNetworkDataRecordHash, newNodeNetworkDataRecord);
                    isAlreadyInsertedFlag = true;
                }
                newNodeNetworkDataRecordMap.put(nodeNetworkDataRecordHash, nodeNetworkDataRecord);
                if (!isAlreadyInsertedFlag && nodeNetworkDataRecord.getRecordTime().equals(newNodeNetworkDataRecordTime)
                        && newNodeNetworkDataRecordNodeStatus != NetworkNodeStatus.INACTIVE) {
                    newNodeNetworkDataRecordMap.put(newNodeNetworkDataRecordHash, newNodeNetworkDataRecord);
                    isAlreadyInsertedFlag = true;
                }
                nodeNetworkDataRecordHash = oldNodeNetworkDataRecordMap.nextKey(nodeNetworkDataRecordHash);
            }
        }
        if (!isAlreadyInsertedFlag) {
            newNodeNetworkDataRecordMap.put(newNodeNetworkDataRecordHash, newNodeNetworkDataRecord);
        }
        return newNodeNetworkDataRecordMap;
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