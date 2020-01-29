package io.coti.nodemanager.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.nodemanager.data.*;
import io.coti.nodemanager.exceptions.NetworkHistoryValidationException;
import io.coti.nodemanager.http.*;
import io.coti.nodemanager.http.data.NodeDailyStatisticsData;
import io.coti.nodemanager.http.data.NodeNetworkResponseData;
import io.coti.nodemanager.http.data.NodeStatisticsData;
import io.coti.nodemanager.model.NodeDailyActivities;
import io.coti.nodemanager.model.NodeHistory;
import io.coti.nodemanager.services.interfaces.INetworkHistoryService;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static java.lang.Math.max;
import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class NetworkHistoryService implements INetworkHistoryService {

    public static final int NUMBER_OF_SECONDS_IN_DAY = 24 * 60 * 60;
    @Autowired
    private NodeHistory nodeHistory;
    @Autowired
    private NodeDailyActivities nodeDailyActivities;

    @Override
    public List<NodeHistoryData> getNodesHistory() {
        List<NodeHistoryData> nodeHistoryDataList = new LinkedList<>();
        nodeHistory.forEach(nodeHistoryDataList::add);
        return nodeHistoryDataList;
    }

    @Override
    public LinkedList<Object> getNodeEvents(GetNodeStatisticsRequest getNodeStatisticsRequest, boolean rawData) {
        Hash nodeHash = getNodeStatisticsRequest.getNodeHash();
        LocalDate startDate = getNodeStatisticsRequest.getStartDate();
        LocalDate endDate = getNodeStatisticsRequest.getEndDate();

        LinkedList<Object> nodeEvents = new LinkedList<>();
        NodeDailyActivityData nodeDailyActivityData = nodeDailyActivities.getByHash(nodeHash);
        if (nodeDailyActivityData == null) {
            return nodeEvents;
        }
        Pair<LocalDate, Hash> beforePeriodEventRef = null;
        ConcurrentSkipListSet<LocalDate> nodeDaySet = nodeDailyActivityData.getNodeDaySet();
        LocalDate localDate = nodeDaySet.ceiling(startDate);
        if (localDate == null) {
            LocalDate prevDate = nodeDaySet.floor(startDate);
            while (prevDate != null && beforePeriodEventRef == null) {
                NodeHistoryData nodeHistoryData = nodeHistory.getByHash(nodeDailyActivityData.calculateNodeHistoryDataHash(prevDate));
                if (nodeHistoryData != null && !nodeHistoryData.getNodeNetworkDataRecordMap().isEmpty()) {
                    LinkedMap<Hash, NodeNetworkDataRecord> nodeHistoryMap = nodeHistoryData.getNodeNetworkDataRecordMap();
                    Hash nodeNetworkDataRecordHash = nodeHistoryMap.lastKey();
                    while (beforePeriodEventRef == null && nodeNetworkDataRecordHash != null) {
                        NodeNetworkDataRecord nodeNetworkDataRecord = nodeHistoryMap.get(nodeNetworkDataRecordHash);
                        if (nodeNetworkDataRecord.getNodeStatus() == NetworkNodeStatus.ACTIVE) {
                            beforePeriodEventRef = new ImmutablePair<>(prevDate, nodeNetworkDataRecordHash);
                        } else if (nodeNetworkDataRecord.getNodeStatus() == NetworkNodeStatus.INACTIVE) {
                            return nodeEvents;
                        }
                        nodeNetworkDataRecordHash = nodeHistoryMap.previousKey(nodeNetworkDataRecordHash);
                    }
                }
                prevDate = nodeDaySet.lower(prevDate);
            }
        } else if (localDate.isAfter(endDate)) {
            NodeHistoryData nodeHistoryData = nodeHistory.getByHash(nodeDailyActivityData.calculateNodeHistoryDataHash(localDate));
            beforePeriodEventRef = nodeHistoryData.getNodeNetworkDataRecordMap().get(nodeHistoryData.getNodeNetworkDataRecordMap().firstKey()).getStatusChainRef();
        } else {
            while (localDate != null && !localDate.isAfter(endDate)) {
                NodeHistoryData nodeHistoryData = nodeHistory.getByHash(nodeDailyActivityData.calculateNodeHistoryDataHash(localDate));
                if (nodeHistoryData != null) {
                    for (LinkedMap.Entry<Hash, NodeNetworkDataRecord> nodeNetworkDataRecordEntry : nodeHistoryData.getNodeNetworkDataRecordMap().entrySet()) {
                        if (nodeNetworkDataRecordEntry.getValue().getNodeStatus() == NetworkNodeStatus.ACTIVE
                                || nodeNetworkDataRecordEntry.getValue().getNodeStatus() == NetworkNodeStatus.INACTIVE) {
                            if (rawData) {
                                nodeEvents.add(nodeNetworkDataRecordEntry.getValue());
                            } else {
                                nodeEvents.add(new NodeNetworkResponseData(nodeNetworkDataRecordEntry.getValue()));
                            }
                            if (beforePeriodEventRef == null) {
                                beforePeriodEventRef = nodeNetworkDataRecordEntry.getValue().getStatusChainRef();
                            }
                        }
                    }
                }
                localDate = nodeDaySet.higher(localDate);
            }
        }

        if (beforePeriodEventRef != null && beforePeriodEventRef.getLeft().isBefore(startDate)) {
            Hash prevDateHashEntryHash = nodeDailyActivityData.calculateNodeHistoryDataHash(beforePeriodEventRef.getLeft());
            NodeHistoryData nodeHistoryData = nodeHistory.getByHash(prevDateHashEntryHash);
            if (nodeHistoryData != null && !nodeHistoryData.getNodeNetworkDataRecordMap().isEmpty()) {
                if (rawData) {
                    nodeEvents.addFirst(nodeHistoryData.getNodeNetworkDataRecordMap().get(beforePeriodEventRef.getRight()));
                } else {
                    NodeNetworkResponseData nodeNetworkResponseData = new NodeNetworkResponseData(nodeHistoryData.getNodeNetworkDataRecordMap().get(beforePeriodEventRef.getRight()));
                    nodeEvents.addFirst(nodeNetworkResponseData);
                }
            }
        }
        return nodeEvents;
    }

    @Override
    public List<NodeDailyStatisticsData> getNodeDailyStats(GetNodeStatisticsRequest getNodeStatisticsRequest) {
        List<NodeDailyStatisticsData> nodeStatisticsList = new LinkedList<>();

        LocalDate today = LocalDate.now(ZoneId.of("UTC"));
        LocalDate startDate = getNodeStatisticsRequest.getStartDate();
        if (startDate.isAfter(today)) {
            return nodeStatisticsList;
        }
        LocalDate endDate = getNodeStatisticsRequest.getEndDate();
        if (endDate.isAfter(today)) {
            endDate = today;
        }

        LinkedList<Object> eventsList = getNodeEvents(getNodeStatisticsRequest, false);
        if (eventsList == null || eventsList.isEmpty()) {
            return nodeStatisticsList;
        }

        NetworkNodeStatus currentNetworkNodeStatus = NetworkNodeStatus.INACTIVE;
        if (((NodeNetworkResponseData) eventsList.getFirst()).getRecordDateTime().atZone(ZoneId.of("UTC")).toLocalDate().isBefore(startDate)) {
            currentNetworkNodeStatus = ((NodeNetworkResponseData) eventsList.getFirst()).getNodeStatus();
        }
        int eventsListIndex = 0;

        for (LocalDate localDate = startDate; !localDate.isAfter(endDate); localDate = localDate.plusDays(1)) {
            long upTime = 0;
            int restarts = 0;
            int downEvents = 0;
            LocalDateTime lastActive = localDate.atStartOfDay();

            while (eventsListIndex < eventsList.size() && eventsList.get(eventsListIndex) != null
                    && ((NodeNetworkResponseData) eventsList.get(eventsListIndex)).getRecordDateTime().toLocalDate().isBefore(localDate)) {
                eventsListIndex += 1;
            }
            while (eventsListIndex < eventsList.size()
                    && ((NodeNetworkResponseData) eventsList.get(eventsListIndex)).getRecordDateTime().toLocalDate().isEqual(localDate)) {
                NodeNetworkResponseData nodeNetworkResponseData = ((NodeNetworkResponseData) eventsList.get(eventsListIndex));
                if (nodeNetworkResponseData.getNodeStatus() == NetworkNodeStatus.ACTIVE) {
                    restarts += 1;
                    if (currentNetworkNodeStatus == NetworkNodeStatus.INACTIVE) {
                        lastActive = nodeNetworkResponseData.getRecordDateTime();
                    }
                    currentNetworkNodeStatus = nodeNetworkResponseData.getNodeStatus();
                } else {
                    downEvents += 1;
                    if (currentNetworkNodeStatus == NetworkNodeStatus.ACTIVE) {
                        upTime += lastActive.until(nodeNetworkResponseData.getRecordDateTime(), ChronoUnit.SECONDS);
                        lastActive = nodeNetworkResponseData.getRecordDateTime();
                    }
                    currentNetworkNodeStatus = nodeNetworkResponseData.getNodeStatus();
                }
                eventsListIndex += 1;
            }
            if (currentNetworkNodeStatus == NetworkNodeStatus.ACTIVE) {
                if (!localDate.isEqual(today)) {
                    upTime += lastActive.until(localDate.plusDays(1).atStartOfDay(), ChronoUnit.SECONDS);
                } else {
                    upTime += lastActive.until(LocalDateTime.now(ZoneId.of("UTC")), ChronoUnit.SECONDS);
                }
            }

            NodeDailyStatisticsData nodeDailyStatisticsData = new NodeDailyStatisticsData(localDate, upTime, restarts, downEvents);
            nodeStatisticsList.add(nodeDailyStatisticsData);
        }
        return nodeStatisticsList;
    }

    @Override
    public NodeStatisticsData getNodeStatsTotal(GetNodeStatisticsRequest getNodeStatisticsRequest) {
        LocalDate today = LocalDate.now(ZoneId.of("UTC"));
        LocalDate startDate = getNodeStatisticsRequest.getStartDate();
        if (startDate.isAfter(today)) {
            return new NodeStatisticsData(0, 0, 0);
        }
        LocalDate endDate = getNodeStatisticsRequest.getEndDate();
        if (endDate.isAfter(today)) {
            endDate = today;
        }

        LinkedList<Object> eventsList = getNodeEvents(getNodeStatisticsRequest, false);
        if (eventsList == null || eventsList.isEmpty()) {
            return new NodeStatisticsData(0, 0, 0);
        }

        NetworkNodeStatus currentNetworkNodeStatus = NetworkNodeStatus.INACTIVE;
        if (((NodeNetworkResponseData) eventsList.getFirst()).getRecordDateTime().toLocalDate().isBefore(startDate)) {
            currentNetworkNodeStatus = ((NodeNetworkResponseData) eventsList.getFirst()).getNodeStatus();
        }

        long upTime = 0;
        int restarts = 0;
        int downEvents = 0;
        LocalDateTime lastActive = startDate.atStartOfDay();

        for (Object nodeNetworkResponseData : eventsList) {
            if (!((NodeNetworkResponseData) nodeNetworkResponseData).getRecordDateTime().isBefore(startDate.atStartOfDay())) {
                if (((NodeNetworkResponseData) nodeNetworkResponseData).getNodeStatus() == NetworkNodeStatus.ACTIVE) {
                    restarts += 1;
                    if (currentNetworkNodeStatus == NetworkNodeStatus.INACTIVE) {
                        lastActive = ((NodeNetworkResponseData) nodeNetworkResponseData).getRecordDateTime();
                    }
                    currentNetworkNodeStatus = ((NodeNetworkResponseData) nodeNetworkResponseData).getNodeStatus();
                } else {
                    downEvents += 1;
                    if (currentNetworkNodeStatus == NetworkNodeStatus.ACTIVE) {
                        upTime += lastActive.until(((NodeNetworkResponseData) nodeNetworkResponseData).getRecordDateTime(), ChronoUnit.SECONDS);
                        lastActive = ((NodeNetworkResponseData) nodeNetworkResponseData).getRecordDateTime();
                    }
                    currentNetworkNodeStatus = ((NodeNetworkResponseData) nodeNetworkResponseData).getNodeStatus();
                }
            }
        }
        if (currentNetworkNodeStatus == NetworkNodeStatus.ACTIVE) {
            if (!endDate.isEqual(today)) {
                upTime += lastActive.until(endDate.plusDays(1).atStartOfDay(), ChronoUnit.SECONDS);
            } else {
                upTime += lastActive.until(LocalDateTime.now(ZoneId.of("UTC")), ChronoUnit.SECONDS);
            }
        }

        return new NodeStatisticsData(upTime, restarts, downEvents);
    }

    @Override
    public ResponseEntity<IResponse> getNodeActivityPercentage(GetNodeStatisticsRequest getNodeStatisticsRequest) {
        try {
            Instant now = Instant.now();
            now = now.minusNanos(now.getNano());
            NodeActivityData nodeActivityData = getNodeActivity(getNodeStatisticsRequest, now);
            long nodeExclusionPeriodInSeconds = getNodeExclusionPeriodInSeconds(getNodeStatisticsRequest, now);
            long activityUpTimeInSeconds = nodeActivityData.getActivityUpTimeInSeconds();
            long numberOfDays = nodeActivityData.getNumberOfDays();
            long maxExpectedUpTimeInSeconds = numberOfDays * NUMBER_OF_SECONDS_IN_DAY - nodeExclusionPeriodInSeconds;
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new GetNodeActivityPercentageResponse(((double) activityUpTimeInSeconds) / (maxExpectedUpTimeInSeconds) * 100));
        } catch (NetworkHistoryValidationException e) {
            return ResponseEntity.badRequest().body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    private long getNodeExclusionPeriodInSeconds(GetNodeStatisticsRequest getNodeStatisticsRequest, Instant now) {
        Hash nodeHash = getNodeStatisticsRequest.getNodeHash();
        LocalDate startDate = getNodeStatisticsRequest.getStartDate();
        LocalDate endDate = getNodeStatisticsRequest.getEndDate();
        long nodeExclusionPeriodInSeconds = 0;

        LocalDate todayLocalDate = LocalDate.now(ZoneId.of("UTC"));
        if (endDate.isEqual(todayLocalDate)) {
            nodeExclusionPeriodInSeconds += max(0, now.until(endDate.plusDays(1).atStartOfDay().atZone(ZoneId.of("UTC")).toInstant(), ChronoUnit.SECONDS));
        }
        NodeNetworkDataRecord nodeNetworkDataRecord = getNodeNetworkFirstDataRecord(nodeHash);
        Instant activationInstant = nodeNetworkDataRecord.getRecordTime();
        LocalDate activationDate = activationInstant.atZone(ZoneId.of("UTC")).toLocalDate();
        if (!startDate.isAfter(activationDate)) {
            nodeExclusionPeriodInSeconds += activationDate.atStartOfDay().atZone(ZoneId.of("UTC")).toInstant().until(activationInstant, ChronoUnit.SECONDS);
        }
        return nodeExclusionPeriodInSeconds;
    }

    @Override
    public ResponseEntity<IResponse> getNodeActivityInSeconds(GetNodeStatisticsRequest getNodeStatisticsRequest) {
        try {
            Instant now = Instant.now();
            now = now.minusNanos(now.getNano());
            NodeActivityData nodeActivityData = getNodeActivity(getNodeStatisticsRequest, now);
            return ResponseEntity.ok()
                    .body(new GetNodeActivityInSecondsResponse(nodeActivityData));
        } catch (NetworkHistoryValidationException e) {
            return ResponseEntity.badRequest().body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    @Override
    public NodeActivityData getNodeActivity(GetNodeStatisticsRequest getNodeStatisticsRequest, Instant now) {
        Hash nodeHash = getNodeStatisticsRequest.getNodeHash();
        LocalDate startDate = getNodeStatisticsRequest.getStartDate();
        LocalDate endDate = getNodeStatisticsRequest.getEndDate();
        if (endDate.isBefore(startDate)) {
            throw new NetworkHistoryValidationException("Invalid dates range Start: " + startDate + " End: " + endDate);
        }
        NodeDailyActivityData nodeDailyActivityData = getNodeDailyActivityData(nodeHash);
        LocalDate todayLocalDate = LocalDate.now(ZoneId.of("UTC"));
        endDate = endDate.isAfter(todayLocalDate) ? todayLocalDate : endDate;

        LocalDate firstDateWithEvent = nodeDailyActivityData.getNodeDaySet().first();
        startDate = getFirstRelevantDate(startDate, endDate, firstDateWithEvent);

        long activityUpTimeInSeconds = getActivityUpTimeInSeconds(startDate, endDate, nodeDailyActivityData, now);
        long numberOfDays = startDate.until(endDate, DAYS) + 1;
        return new NodeActivityData(activityUpTimeInSeconds, numberOfDays);
    }

    private NodeDailyActivityData getNodeDailyActivityData(Hash nodeHash) {
        NodeDailyActivityData nodeDailyActivityData = nodeDailyActivities.getByHash(nodeHash);
        if (nodeDailyActivityData == null) {
            throw new NetworkHistoryValidationException("Invalid node hash " + nodeHash);
        }
        return nodeDailyActivityData;
    }

    @Override
    public ResponseEntity<IResponse> getNodeActivityInSecondsByDay(GetNodeStatisticsRequest getNodeStatisticsRequest) {
        try {
            Map<LocalDate, Pair> upTimesByDates = getNodeActivityPerDay(getNodeStatisticsRequest);
            return ResponseEntity.ok()
                    .body(new GetNodeActivityInSecondsPerDaysResponse(upTimesByDates));
        } catch (NetworkHistoryValidationException e) {
            return ResponseEntity.badRequest().body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    @Override
    public ResponseEntity<IResponse> getNodeActivationTime(GetNodeActivationTimeRequest getNodeActivationTimeRequest) {
        try {
            Hash nodeHash = getNodeActivationTimeRequest.getNodeHash();
            NodeNetworkDataRecord nodeNetworkDataRecord = getNodeNetworkFirstDataRecord(nodeHash);
            NodeNetworkDataRecord originalActivationDataRecord = nodeNetworkDataRecord;
            if (nodeNetworkDataRecord.isNotOriginalEvent()) {
                originalActivationDataRecord = getOriginalActivationEventRecord(nodeHash);
            }
            return ResponseEntity.ok()
                    .body(new GetNodeActivationTimeResponse(nodeNetworkDataRecord.getRecordTime(), originalActivationDataRecord.getRecordTime()));
        } catch (NetworkHistoryValidationException e) {
            return ResponseEntity.badRequest().body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    private NodeNetworkDataRecord getOriginalActivationEventRecord(Hash nodeHash) {
        NodeNetworkDataRecord originalActivationEventRecord = null;
        NodeDailyActivityData nodeDailyActivityData = nodeDailyActivities.getByHash(nodeHash);
        if (nodeDailyActivityData == null) {
            throw new NetworkHistoryValidationException("Node hash does not have activity");
        }
        for (LocalDate localDate : nodeDailyActivityData.getNodeDaySet()) {
            Hash localDateWithEventHash =
                    calculateNodeHistoryDataHash(nodeDailyActivityData.getNodeHash(), localDate);
            NodeHistoryData nodeHistoryByHash = nodeHistory.getByHash(localDateWithEventHash);
            Optional<Map.Entry<Hash, NodeNetworkDataRecord>> hashNodeNetworkDataRecordEntry = nodeHistoryByHash.getNodeNetworkDataRecordMap().entrySet().stream()
                    .filter(nodeNetworkDataRecord ->
                            nodeNetworkDataRecord.getValue().getNodeStatus().equals(NetworkNodeStatus.ACTIVE)
                                    && !nodeNetworkDataRecord.getValue().isNotOriginalEvent()
                    ).findFirst();
            if (hashNodeNetworkDataRecordEntry.isPresent()) {
                originalActivationEventRecord = hashNodeNetworkDataRecordEntry.get().getValue();
                break;
            }
        }
        return originalActivationEventRecord;
    }

    private NodeNetworkDataRecord getNodeNetworkFirstDataRecord(Hash nodeHash) {
        NodeDailyActivityData nodeDailyActivityData = nodeDailyActivities.getByHash(nodeHash);
        if (nodeDailyActivityData == null) {
            throw new NetworkHistoryValidationException("Node hash does not have activity");
        }
        Hash firstDateWithEventHash =
                calculateNodeHistoryDataHash(nodeDailyActivityData.getNodeHash(), nodeDailyActivityData.getNodeDaySet().first());
        NodeHistoryData firstDateWithEventNodeHistoryData = nodeHistory.getByHash(firstDateWithEventHash);
        return firstDateWithEventNodeHistoryData.getNodeNetworkDataRecordMap()
                .get(firstDateWithEventNodeHistoryData.getNodeNetworkDataRecordMap().firstKey());
    }

    private Map<LocalDate, Pair> getNodeActivityPerDay(GetNodeStatisticsRequest getNodeStatisticsRequest) {
        Map<LocalDate, Pair> nodeActivityPerDayMap = new LinkedHashMap<>();

        Hash nodeHash = getNodeStatisticsRequest.getNodeHash();
        LocalDate requestedStartDate = getNodeStatisticsRequest.getStartDate();
        LocalDate requestedEndDate = getNodeStatisticsRequest.getEndDate();
        if (requestedEndDate.isBefore(requestedStartDate)) {
            throw new NetworkHistoryValidationException("Invalid dates range Start: " + requestedStartDate + " End: " + requestedEndDate);
        }
        LocalDate todayLocalDate = LocalDate.now(ZoneId.of("UTC"));

        NodeDailyActivityData nodeDailyActivityData = getNodeDailyActivityData(nodeHash);

        LocalDate endDate = requestedEndDate.isAfter(todayLocalDate) ? todayLocalDate : requestedEndDate;
        LocalDate firstDateWithEvent = nodeDailyActivityData.getNodeDaySet().first();
        LocalDate startDate = getFirstRelevantDate(requestedStartDate, endDate, firstDateWithEvent);

        long numOfRequestedDaysPriorNodeCreation = DAYS.between(requestedStartDate, firstDateWithEvent);
        for (int extraDays = 0; extraDays < numOfRequestedDaysPriorNodeCreation; extraDays++) {
            nodeActivityPerDayMap.put(requestedStartDate.plusDays(extraDays), Pair.of(0, 0));
        }

        LocalDate localDate = startDate;
        while (!localDate.isAfter(endDate)) {
            long dayUpTimeInSeconds = getActivityUpTimeInSeconds(localDate, localDate, nodeDailyActivityData, Instant.now());
            nodeActivityPerDayMap.put(localDate, Pair.of(dayUpTimeInSeconds, NUMBER_OF_SECONDS_IN_DAY - dayUpTimeInSeconds));
            localDate = localDate.plusDays(1);
        }

        long numOfRequestedDaysInFuture = DAYS.between(endDate, requestedEndDate);
        for (int extraDays = 1; extraDays <= numOfRequestedDaysInFuture; extraDays++) {
            nodeActivityPerDayMap.put(endDate.plusDays(extraDays), Pair.of(0, 0));
        }

        return nodeActivityPerDayMap;
    }

    private LocalDate getFirstRelevantDate(LocalDate requestedStartDate, LocalDate endDate, LocalDate firstDateWithEvent) {
        LocalDate startDate = requestedStartDate.isBefore(firstDateWithEvent) ? firstDateWithEvent : requestedStartDate;
        if (endDate.isBefore(startDate)) {
            throw new NetworkHistoryValidationException("Invalid dates range End: " + endDate + " before node started at: " + startDate);
        }
        return startDate;
    }

    private long getActivityUpTimeInSeconds(LocalDate startDate, LocalDate endDate, NodeDailyActivityData nodeDailyActivityData, Instant now) {
        long activityUpTimeInSeconds = 0;
        LocalDate ceilingDate = nodeDailyActivityData.getNodeDaySet().ceiling(endDate);
        LocalDate lastDateWithEvent = Optional.ofNullable(ceilingDate).orElse(nodeDailyActivityData.getNodeDaySet().last());
        Hash lastDateWithEventHash = calculateNodeHistoryDataHash(nodeDailyActivityData.getNodeHash(), lastDateWithEvent);
        NodeHistoryData lastDateWithEventNodeHistoryData = nodeHistory.getByHash(lastDateWithEventHash);
        LinkedMap<Hash, NodeNetworkDataRecord> lastRelevantNodeNetworkRecordMap = lastDateWithEventNodeHistoryData.getNodeNetworkDataRecordMap();
        NodeNetworkDataRecord lastRelevantNodeNetworkDataRecord;
        Instant endInstant;
        Instant startInstant = localDateToInstant(startDate);
        if (!lastDateWithEvent.isAfter(endDate)) {
            lastRelevantNodeNetworkDataRecord = lastRelevantNodeNetworkRecordMap.get(lastRelevantNodeNetworkRecordMap.lastKey());
            if (!lastRelevantNodeNetworkDataRecord.getNodeStatus().equals(NetworkNodeStatus.ACTIVE)) {
                endInstant = lastRelevantNodeNetworkDataRecord.getRecordTime();
            } else {
                endInstant = LocalDate.now(ZoneId.of("UTC")).equals(endDate) ? now : localDateToInstant(endDate.plusDays(1));
            }
        } else {
            lastRelevantNodeNetworkDataRecord = lastRelevantNodeNetworkRecordMap.get(lastRelevantNodeNetworkRecordMap.firstKey());
            endInstant = localDateToInstant(endDate.plusDays(1));
        }
        lastRelevantNodeNetworkRecordMap = getNodeNetworkDataRecordMap(lastRelevantNodeNetworkDataRecord, lastRelevantNodeNetworkRecordMap);
        NodeNetworkDataRecord nodeNetworkDataRecordByChainRef = getNodeNetworkDataRecordByChainRef(lastRelevantNodeNetworkDataRecord, lastRelevantNodeNetworkRecordMap);
        if (nodeNetworkDataRecordByChainRef == null || !nodeNetworkDataRecordByChainRef.getNodeStatus().equals(NetworkNodeStatus.ACTIVE)) {
            nodeNetworkDataRecordByChainRef = lastRelevantNodeNetworkDataRecord;
        }
        if (nodeNetworkDataRecordByChainRef.getRecordTime().isAfter(startInstant)) {
            startInstant = nodeNetworkDataRecordByChainRef.getRecordTime();
        }
        if (startInstant.isBefore(endInstant)) {
            activityUpTimeInSeconds += startInstant.until(endInstant, ChronoUnit.SECONDS);
        }
        lastRelevantNodeNetworkRecordMap = getNodeNetworkDataRecordMap(nodeNetworkDataRecordByChainRef, lastRelevantNodeNetworkRecordMap);
        lastRelevantNodeNetworkDataRecord = getNodeNetworkDataRecordByChainRef(nodeNetworkDataRecordByChainRef, lastRelevantNodeNetworkRecordMap);

        while (lastRelevantNodeNetworkDataRecord != null && startInstant != localDateToInstant(startDate) && lastRelevantNodeNetworkDataRecord.getRecordTime().isAfter(localDateToInstant(startDate))) {
            startInstant = localDateToInstant(startDate);
            endInstant = lastRelevantNodeNetworkDataRecord.getRecordTime();
            lastRelevantNodeNetworkRecordMap = getNodeNetworkDataRecordMap(lastRelevantNodeNetworkDataRecord, lastRelevantNodeNetworkRecordMap);
            nodeNetworkDataRecordByChainRef = getNodeNetworkDataRecordByChainRef(lastRelevantNodeNetworkDataRecord, lastRelevantNodeNetworkRecordMap);
            if (nodeNetworkDataRecordByChainRef.getRecordTime().isAfter(startInstant)) {
                startInstant = nodeNetworkDataRecordByChainRef.getRecordTime();
            }
            activityUpTimeInSeconds += startInstant.until(endInstant, ChronoUnit.SECONDS);
            lastRelevantNodeNetworkRecordMap = getNodeNetworkDataRecordMap(nodeNetworkDataRecordByChainRef, lastRelevantNodeNetworkRecordMap);
            lastRelevantNodeNetworkDataRecord = getNodeNetworkDataRecordByChainRef(nodeNetworkDataRecordByChainRef, lastRelevantNodeNetworkRecordMap);
        }
        return activityUpTimeInSeconds;
    }

    private LinkedMap<Hash, NodeNetworkDataRecord> getNodeNetworkDataRecordMap(NodeNetworkDataRecord nodeNetworkDataRecord, LinkedMap<Hash, NodeNetworkDataRecord> nodeNetworkDataRecordMap) {
        Pair<LocalDate, Hash> chainRef = nodeNetworkDataRecord.getStatusChainRef();
        if (chainRef == null) {
            return null;
        }
        LocalDate localDate = chainRef.getLeft();
        if (!nodeNetworkDataRecord.getRecordTime().atZone(ZoneId.of("UTC")).toLocalDate().isEqual(localDate)) {
            nodeNetworkDataRecordMap = nodeHistory.getByHash(calculateNodeHistoryDataHash(nodeNetworkDataRecord)).getNodeNetworkDataRecordMap();
        }
        return nodeNetworkDataRecordMap;
    }

    private NodeNetworkDataRecord getNodeNetworkDataRecordByChainRef(NodeNetworkDataRecord nodeNetworkDataRecord, LinkedMap<Hash, NodeNetworkDataRecord> nodeNetworkDataRecordMap) {
        Pair<LocalDate, Hash> chainRef = nodeNetworkDataRecord.getStatusChainRef();
        if (chainRef == null) {
            return null;
        }
        Hash recordHash = chainRef.getRight();
        return nodeNetworkDataRecordMap.get(recordHash);
    }

    public NodeNetworkDataRecord getNodeNetworkDataRecordByChainRef(NodeNetworkDataRecord nodeNetworkDataRecord) {
        Pair<LocalDate, Hash> chainRef = nodeNetworkDataRecord.getStatusChainRef();
        if (chainRef == null) {
            return null;
        }
        Hash recordHash = chainRef.getRight();
        return nodeHistory.getByHash(calculateNodeHistoryDataHash(nodeNetworkDataRecord)).getNodeNetworkDataRecordMap().get(recordHash);
    }

    private Hash calculateNodeHistoryDataHash(NodeNetworkDataRecord nodeNetworkDataRecord) {
        Hash nodeHash = nodeNetworkDataRecord.getNetworkNodeData().getNodeHash();
        LocalDate localDate = nodeNetworkDataRecord.getStatusChainRef().getLeft();
        return calculateNodeHistoryDataHash(nodeHash, localDate);
    }

    public Hash calculateNodeHistoryDataHash(Hash nodeHash, LocalDate localDate) {
        return new Hash(ByteBuffer.allocate(nodeHash.getBytes().length + Long.BYTES).
                put(nodeHash.getBytes()).putLong(localDate.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()).array());
    }

    private Instant localDateToInstant(LocalDate localDate) {
        return localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    public NodeNetworkDataRecord getLastNodeNetworkDataRecord(NodeHistoryData nodeHistoryData) {
        return nodeHistoryData.getNodeNetworkDataRecordMap().get(nodeHistoryData.getNodeNetworkDataRecordMap().lastKey());
    }

}
