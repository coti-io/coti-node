package io.coti.nodemanager.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.nodemanager.data.*;
import io.coti.nodemanager.exceptions.NetworkHistoryValidationException;
import io.coti.nodemanager.http.*;
import io.coti.nodemanager.http.data.*;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;
import static io.coti.nodemanager.http.HttpStringConstants.*;
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
    public ResponseEntity<IResponse> getNodeEventsResponse(GetNodeStatisticsRequest getNodeStatisticsRequest) {
        try {
            List<NodeNetworkDataRecord> nodeEvents = getNodeEvents(getNodeStatisticsRequest).getEvents();
            List<NodeNetworkRecordResponseData> nodeNetworkRecordResponseDataList = new LinkedList<>();
            nodeEvents.forEach(nodeNetworkDataRecord -> nodeNetworkRecordResponseDataList.add(new NodeNetworkRecordResponseData(nodeNetworkDataRecord)));
            return ResponseEntity.ok(new GetNodeEventStatisticsResponse(nodeNetworkRecordResponseDataList));
        } catch (NetworkHistoryValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(e.getMessage(), STATUS_ERROR));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(String.format(NODE_EVENTS_SERVER_ERROR, e.getMessage()), STATUS_ERROR));
        }
    }

    private NodeEventsData getNodeEvents(GetNodeStatisticsRequest getNodeStatisticsRequest) {
        Hash nodeHash = getNodeStatisticsRequest.getNodeHash();
        LocalDate startDate = getNodeStatisticsRequest.getStartDate();
        LocalDate endDate = getNodeStatisticsRequest.getEndDate();

        LocalDate today = LocalDate.now(ZoneId.of("UTC"));

        if (startDate.isAfter(endDate) || (startDate.isAfter(today))) {
            throw new NetworkHistoryValidationException(String.format(INVALID_DATE_RANGE, startDate, endDate));
        }

        endDate = endDate.isAfter(today) ? today : endDate;

        NodeDailyActivityData nodeDailyActivityData = nodeDailyActivities.getByHash(nodeHash);
        if (nodeDailyActivityData == null) {
            throw new NetworkHistoryValidationException(String.format(NODE_INVALID_HASH, getNodeStatisticsRequest.getNodeHash()));
        }

        LinkedList<NodeNetworkDataRecord> nodeEvents = new LinkedList<>();
        NodeNetworkDataRecord previousEvent = null;
        NodeNetworkDataRecord nextEvent = null;

        ConcurrentSkipListSet<LocalDate> nodeDaySet = nodeDailyActivityData.getNodeDaySet();
        LocalDate localDate = nodeDaySet.ceiling(startDate);
        while (localDate != null && !localDate.isAfter(endDate)) {
            NodeHistoryData nodeHistoryData = getNodeHistoryData(nodeHash, localDate);
            if (nodeHistoryData != null) {
                for (Map.Entry<Hash, NodeNetworkDataRecord> nodeNetworkDataRecordEntry : nodeHistoryData.getNodeNetworkDataRecordMap().entrySet()) {
                    nodeEvents.add(nodeNetworkDataRecordEntry.getValue());
                }
            }
            localDate = nodeDaySet.higher(localDate);
        }
        if (localDate != null && localDate.isAfter(endDate)) {
            nextEvent = getFirstNodeNetworkDataRecord(nodeHash, localDate);
        }
        LocalDate previousDate = nodeDaySet.lower(startDate);
        if (previousDate != null) {
            previousEvent = getLastNodeNetworkDataRecord(nodeHash, previousDate);
        }
        return new NodeEventsData(nodeEvents, previousEvent, nextEvent);
    }

    @Override
    public ResponseEntity<IResponse> getNodeDailyStats(GetNodeStatisticsRequest getNodeStatisticsRequest) {
        try {
            LocalDate startDate = getNodeStatisticsRequest.getStartDate();
            LocalDate endDate = getNodeStatisticsRequest.getEndDate();

            NodeEventsData nodeEventsData = getNodeEvents(getNodeStatisticsRequest);
            List<NodeNetworkDataRecord> nodeEvents = nodeEventsData.getEvents();
            NodeNetworkDataRecord previousEvent = nodeEventsData.getPreviousEvent();

            List<NodeDailyStatisticsData> nodeStatisticsList = new LinkedList<>();

            Instant now = Instant.now();
            LocalDate today = LocalDate.now(ZoneId.of("UTC"));

            NetworkNodeStatus previousNetworkNodeStatus = NetworkNodeStatus.INACTIVE;
            if (previousEvent != null) {
                previousNetworkNodeStatus = previousEvent.getNodeStatus();
            }

            int eventsListIndex = 0;

            for (LocalDate localDate = startDate; !localDate.isAfter(endDate); localDate = localDate.plusDays(1)) {
                long upTimeInSeconds = 0;
                int restarts = 0;
                int downEvents = 0;
                Instant activeInstant = localDateToInstant(localDate);

                while (eventsListIndex < nodeEvents.size()
                        && (nodeEvents.get(eventsListIndex).getRecordTime().atZone(ZoneId.of("UTC")).toLocalDate().isEqual(localDate))) {
                    NodeNetworkDataRecord nodeNetworkDataRecord = nodeEvents.get(eventsListIndex);
                    NetworkNodeStatus currentNodeStatus = nodeNetworkDataRecord.getNodeStatus();
                    if (currentNodeStatus.equals(NetworkNodeStatus.ACTIVE)) {
                        restarts += 1;
                        if (previousNetworkNodeStatus.equals(NetworkNodeStatus.INACTIVE)) {
                            activeInstant = nodeNetworkDataRecord.getRecordTime();
                        }
                    } else {
                        downEvents += 1;
                        if (previousNetworkNodeStatus.equals(NetworkNodeStatus.ACTIVE)) {
                            upTimeInSeconds += activeInstant.until(nodeNetworkDataRecord.getRecordTime(), ChronoUnit.SECONDS);
                        }
                    }
                    previousNetworkNodeStatus = currentNodeStatus;
                    eventsListIndex += 1;
                }
                if (previousNetworkNodeStatus.equals(NetworkNodeStatus.ACTIVE)) {
                    upTimeInSeconds += activeInstant.until(localDate.isEqual(today) ? now : localDateToInstant(localDate.plusDays(1)), ChronoUnit.SECONDS);
                }

                NodeDailyStatisticsData nodeDailyStatisticsData = new NodeDailyStatisticsData(localDate, upTimeInSeconds, restarts, downEvents);
                nodeStatisticsList.add(nodeDailyStatisticsData);
            }
            return ResponseEntity.ok(new GetNodeDailyStatisticsResponse((nodeStatisticsList)));

        } catch (NetworkHistoryValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(e.getMessage(), STATUS_ERROR));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(String.format(NODE_DAILY_STATS_SERVER_ERROR, e.getMessage()), STATUS_ERROR));
        }
    }

    @Override
    public ResponseEntity<IResponse> getNodeStatsTotal(GetNodeStatisticsRequest getNodeStatisticsRequest) {
        try {
            LocalDate startDate = getNodeStatisticsRequest.getStartDate();
            LocalDate endDate = getNodeStatisticsRequest.getEndDate();

            NodeEventsData nodeEventsData = getNodeEvents(getNodeStatisticsRequest);
            List<NodeNetworkDataRecord> nodeEvents = nodeEventsData.getEvents();
            NodeNetworkDataRecord previousEvent = nodeEventsData.getPreviousEvent();

            Instant now = Instant.now();
            LocalDate today = LocalDate.now(ZoneId.of("UTC"));

            NetworkNodeStatus previousNetworkNodeStatus = NetworkNodeStatus.INACTIVE;
            if (previousEvent != null) {
                previousNetworkNodeStatus = previousEvent.getNodeStatus();
            }

            long upTimeInSeconds = 0;
            int restarts = 0;
            int downEvents = 0;
            Instant activeInstant = localDateToInstant(startDate);

            for (NodeNetworkDataRecord nodeNetworkDataRecord : nodeEvents) {
                NetworkNodeStatus currentNodeStatus = nodeNetworkDataRecord.getNodeStatus();
                if (currentNodeStatus.equals(NetworkNodeStatus.ACTIVE)) {
                    restarts += 1;
                    if (previousNetworkNodeStatus.equals(NetworkNodeStatus.INACTIVE)) {
                        activeInstant = nodeNetworkDataRecord.getRecordTime();
                    }
                } else {
                    downEvents += 1;
                    if (previousNetworkNodeStatus.equals(NetworkNodeStatus.ACTIVE)) {
                        upTimeInSeconds += activeInstant.until(nodeNetworkDataRecord.getRecordTime(), ChronoUnit.SECONDS);
                    }
                }
                previousNetworkNodeStatus = currentNodeStatus;
            }
            if (previousNetworkNodeStatus.equals(NetworkNodeStatus.ACTIVE)) {
                upTimeInSeconds += activeInstant.until(endDate.equals(today) ? now : localDateToInstant(endDate.plusDays(1)), ChronoUnit.SECONDS);
            }

            return ResponseEntity.ok(new GetNodeStatisticsResponse(new NodeStatisticsData(upTimeInSeconds, restarts, downEvents)));
        } catch (NetworkHistoryValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(e.getMessage(), STATUS_ERROR));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new Response(String.format(NODE_EVENTS_SERVER_ERROR, e.getMessage()), STATUS_ERROR));
        }
    }

    @Override
    public ResponseEntity<IResponse> getNodesActivityPercentage(GetNodesActivityPercentageRequest getNodesActivityPercentageRequest) {
        Instant now = Instant.now();
        now = now.minusNanos(now.getNano());
        LocalDate startDate = getNodesActivityPercentageRequest.getStartDate();
        LocalDate endDate = getNodesActivityPercentageRequest.getEndDate();
        Map<Hash, NodeActivityPercentageData> nodeHashToActivityPercentage = new HashMap<>();

        final Instant finalNow = now;
        getNodesActivityPercentageRequest.getNodeHashes().forEach(nodeHash -> {
            try {
                NodeActivityData nodeActivityData = getNodeActivity(nodeHash, startDate, endDate, finalNow);
                long nodeExclusionPeriodInSeconds = getNodeExclusionPeriodInSeconds(nodeHash, startDate, endDate, finalNow);
                double percentage = getPercentage(nodeActivityData, nodeExclusionPeriodInSeconds);
                nodeHashToActivityPercentage.put(nodeHash, new NodeActivityPercentageData(percentage));
            } catch (NetworkHistoryValidationException e) {
                nodeHashToActivityPercentage.put(nodeHash, new NodeActivityPercentageErrorData(e.getMessage()));
            }
        });
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GetNodesActivityPercentageResponse(nodeHashToActivityPercentage));
    }

    private double getPercentage(NodeActivityData nodeActivityData, long nodeExclusionPeriodInSeconds) {
        long activityUpTimeInSeconds = nodeActivityData.getActivityUpTimeInSeconds();
        long numberOfDays = nodeActivityData.getNumberOfDays();
        long maxExpectedUpTimeInSeconds = numberOfDays * NUMBER_OF_SECONDS_IN_DAY - nodeExclusionPeriodInSeconds;
        return ((double) activityUpTimeInSeconds) / (maxExpectedUpTimeInSeconds) * 100;
    }

    @Override
    public ResponseEntity<IResponse> getNodeActivityPercentage(GetNodeStatisticsRequest getNodeStatisticsRequest) {
        try {
            Instant now = Instant.now();
            now = now.minusNanos(now.getNano());
            NodeActivityData nodeActivityData = getNodeActivity(getNodeStatisticsRequest, now);
            long nodeExclusionPeriodInSeconds = getNodeExclusionPeriodInSeconds(getNodeStatisticsRequest, now);
            double percentage = getPercentage(nodeActivityData, nodeExclusionPeriodInSeconds);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new GetNodeActivityPercentageResponse(percentage));
        } catch (NetworkHistoryValidationException e) {
            return ResponseEntity.badRequest().body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    private long getNodeExclusionPeriodInSeconds(GetNodeStatisticsRequest getNodeStatisticsRequest, Instant now) {
        return getNodeExclusionPeriodInSeconds(getNodeStatisticsRequest.getNodeHash(), getNodeStatisticsRequest.getStartDate(), getNodeStatisticsRequest.getEndDate(), now);
    }

    private long getNodeExclusionPeriodInSeconds(Hash nodeHash, LocalDate startDate, LocalDate endDate, Instant now) {
        long nodeExclusionPeriodInSeconds = 0;

        LocalDate todayLocalDate = LocalDate.now(ZoneId.of("UTC"));
        if (!endDate.isBefore(todayLocalDate)) {
            nodeExclusionPeriodInSeconds += max(0, now.until(todayLocalDate.plusDays(1).atStartOfDay().atZone(ZoneId.of("UTC")).toInstant(), ChronoUnit.SECONDS));
        }
        NodeNetworkDataRecord nodeNetworkDataRecord = getNodeNetworkFirstDataRecord(nodeHash);
        Instant activationInstant = nodeNetworkDataRecord.getRecordTime();
        LocalDate activationDate = activationInstant.atZone(ZoneId.of("UTC")).toLocalDate();
        if (!startDate.isAfter(activationDate)) {
            nodeExclusionPeriodInSeconds += localDateToInstant(activationDate).until(activationInstant, ChronoUnit.SECONDS);
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
        return getNodeActivity(getNodeStatisticsRequest.getNodeHash(), getNodeStatisticsRequest.getStartDate(), getNodeStatisticsRequest.getEndDate(), now);
    }

    private NodeActivityData getNodeActivity(Hash nodeHash, LocalDate startDate, LocalDate endDate, Instant now) {
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
            Map<LocalDate, NodeDailyActivityResponseData> upTimesByDates = getNodeActivityPerDay(getNodeStatisticsRequest);
            return ResponseEntity.ok()
                    .body(new GetNodeActivityInSecondsPerDaysResponse(upTimesByDates));
        } catch (NetworkHistoryValidationException e) {
            return ResponseEntity.badRequest().body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    @Override
    public ResponseEntity<IResponse> getNodeActivationTime(GetNodeDetailsRequest getNodeDetailsRequest) {
        try {
            Hash nodeHash = getNodeDetailsRequest.getNodeHash();
            NodeNetworkDataRecord firstActivationDataRecord = getNodeNetworkFirstDataRecord(nodeHash);
            NodeNetworkDataRecord originalActivationDataRecord = getOriginalActivationEventRecord(nodeHash, firstActivationDataRecord);

            return ResponseEntity.ok()
                    .body(new GetNodeActivationTimeResponse(firstActivationDataRecord.getRecordTime(), originalActivationDataRecord.getRecordTime()));
        } catch (NetworkHistoryValidationException e) {
            return ResponseEntity.badRequest().body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }


    private NodeNetworkDataRecord getOriginalActivationEventRecord(Hash nodeHash, NodeNetworkDataRecord firstActivationDataRecord) {
        NodeNetworkDataRecord originalActivationEventRecord = firstActivationDataRecord;
        if (firstActivationDataRecord.isNotOriginalEvent()) {
            NodeDailyActivityData nodeDailyActivityData = nodeDailyActivities.getByHash(nodeHash);
            if (nodeDailyActivityData == null) {
                throw new NetworkHistoryValidationException("Node hash does not have activity");
            }
            for (LocalDate localDate : nodeDailyActivityData.getNodeDaySet()) {
                NodeHistoryData nodeHistoryData = getNodeHistoryData(nodeDailyActivityData.getNodeHash(), localDate);
                Optional<Map.Entry<Hash, NodeNetworkDataRecord>> hashNodeNetworkDataRecordEntry = nodeHistoryData.getNodeNetworkDataRecordMap().entrySet().stream()
                        .filter(nodeNetworkDataRecord ->
                                nodeNetworkDataRecord.getValue().getNodeStatus().equals(NetworkNodeStatus.ACTIVE)
                                        && !nodeNetworkDataRecord.getValue().isNotOriginalEvent()
                        ).findFirst();
                if (hashNodeNetworkDataRecordEntry.isPresent()) {
                    originalActivationEventRecord = hashNodeNetworkDataRecordEntry.get().getValue();
                    break;
                }
            }
        }
        return originalActivationEventRecord;
    }

    private NodeNetworkDataRecord getNodeNetworkFirstDataRecord(Hash nodeHash) {
        NodeDailyActivityData nodeDailyActivityData = nodeDailyActivities.getByHash(nodeHash);
        if (nodeDailyActivityData == null) {
            throw new NetworkHistoryValidationException("Node hash does not have activity");
        }
        NodeHistoryData firstDateWithEventNodeHistoryData = getNodeHistoryData(nodeDailyActivityData.getNodeHash(), nodeDailyActivityData.getNodeDaySet().first());
        return firstDateWithEventNodeHistoryData.getNodeNetworkDataRecordMap()
                .get(firstDateWithEventNodeHistoryData.getNodeNetworkDataRecordMap().firstKey());
    }

    private Map<LocalDate, NodeDailyActivityResponseData> getNodeActivityPerDay(GetNodeStatisticsRequest getNodeStatisticsRequest) {
        Map<LocalDate, NodeDailyActivityResponseData> nodeActivityPerDayMap = new LinkedHashMap<>();

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
            nodeActivityPerDayMap.put(requestedStartDate.plusDays(extraDays), new NodeDailyActivityResponseData(0, 0));
        }

        LocalDate localDate = startDate;
        while (!localDate.isAfter(endDate)) {
            long dayUpTimeInSeconds = getActivityUpTimeInSeconds(localDate, localDate, nodeDailyActivityData, Instant.now());
            nodeActivityPerDayMap.put(localDate, new NodeDailyActivityResponseData(dayUpTimeInSeconds, NUMBER_OF_SECONDS_IN_DAY - dayUpTimeInSeconds));
            localDate = localDate.plusDays(1);
        }

        long numOfRequestedDaysInFuture = DAYS.between(endDate, requestedEndDate);
        for (int extraDays = 1; extraDays <= numOfRequestedDaysInFuture; extraDays++) {
            nodeActivityPerDayMap.put(endDate.plusDays(extraDays), new NodeDailyActivityResponseData(0, 0));
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
        NodeHistoryData lastDateWithEventNodeHistoryData = getNodeHistoryData(nodeDailyActivityData.getNodeHash(), lastDateWithEvent);
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
            if (nodeNetworkDataRecordByChainRef == null) {
                break;
            }
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
        if (nodeNetworkDataRecordMap == null) {
            return null;
        }
        Pair<LocalDate, Hash> chainRef = nodeNetworkDataRecord.getStatusChainRef();
        if (chainRef == null) {
            return null;
        }
        Hash recordHash = chainRef.getRight();
        return nodeNetworkDataRecordMap.get(recordHash);
    }

    @Override
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

    @Override
    public Hash calculateNodeHistoryDataHash(Hash nodeHash, LocalDate localDate) {
        return new Hash(ByteBuffer.allocate(nodeHash.getBytes().length + Long.BYTES).
                put(nodeHash.getBytes()).putLong(localDateToInstant(localDate).toEpochMilli()).array());
    }

    private Instant localDateToInstant(LocalDate localDate) {
        return localDate.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    @Override
    public Pair<LocalDate, Hash> getReferenceToRecord(NodeNetworkDataRecord nodeNetworkDataRecord) {
        return new ImmutablePair<>(nodeNetworkDataRecord.getRecordTime().atZone(ZoneId.of("UTC")).toLocalDate(), nodeNetworkDataRecord.getHash());
    }

    @Override
    public ResponseEntity<IResponse> getNodeLastEvent(GetNodeDetailsRequest getNodeDetailsRequest) {
        Hash nodeHash = getNodeDetailsRequest.getNodeHash();
        NodeNetworkDataRecord lastEvent = getLastNodeNetworkDataRecord(nodeHash);
        if (lastEvent == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response(String.format(NODE_INVALID_HASH, nodeHash), STATUS_ERROR));
        }
        return ResponseEntity.ok(new GetNodeLastEventResponse(new NodeNetworkRecordResponseData(lastEvent)));
    }

    @Override
    public NodeNetworkDataRecord getLastNodeNetworkDataRecord(Hash nodeHash) {
        NodeNetworkDataRecord nodeNetworkDataRecord = null;
        NodeDailyActivityData nodeDailyActivityData = nodeDailyActivities.getByHash(nodeHash);
        if (nodeDailyActivityData != null) {
            nodeNetworkDataRecord = getLastNodeNetworkDataRecord(nodeHash, nodeDailyActivityData);
        }
        return nodeNetworkDataRecord;
    }

    @Override
    public NodeNetworkDataRecord getLastNodeNetworkDataRecord(Hash nodeHash, NodeDailyActivityData nodeDailyActivityData) {
        NodeNetworkDataRecord nodeNetworkDataRecord = null;
        ConcurrentSkipListSet<LocalDate> nodeDaySet = nodeDailyActivityData.getNodeDaySet();
        if (!nodeDaySet.isEmpty()) {
            LocalDate lastLocalDate = nodeDailyActivityData.getNodeDaySet().last();
            NodeHistoryData nodeHistoryData = getNodeHistoryData(nodeHash, lastLocalDate);
            nodeNetworkDataRecord = getLastNodeNetworkDataRecord(nodeHistoryData);
        }
        return nodeNetworkDataRecord;
    }

    @Override
    public NodeNetworkDataRecord getLastNodeNetworkDataRecord(NodeHistoryData nodeHistoryData) {
        return nodeHistoryData.getNodeNetworkDataRecordMap().get(nodeHistoryData.getNodeNetworkDataRecordMap().lastKey());
    }

    @Override
    public NodeNetworkDataRecord getFirstNodeNetworkDataRecord(NodeHistoryData nodeHistoryData) {
        return nodeHistoryData.getNodeNetworkDataRecordMap().get(nodeHistoryData.getNodeNetworkDataRecordMap().firstKey());
    }

    @Override
    public NodeHistoryData getNodeHistoryData(Hash nodeHash, LocalDate localDate) {
        Hash nodeHistoryDataHashForEvent = calculateNodeHistoryDataHash(nodeHash, localDate);
        return nodeHistory.getByHash(nodeHistoryDataHashForEvent);
    }

    private NodeNetworkDataRecord getLastNodeNetworkDataRecord(Hash nodeHash, LocalDate localDate) {
        return getLastNodeNetworkDataRecord(getNodeHistoryData(nodeHash, localDate));
    }

    private NodeNetworkDataRecord getFirstNodeNetworkDataRecord(Hash nodeHash, LocalDate localDate) {
        return getFirstNodeNetworkDataRecord(getNodeHistoryData(nodeHash, localDate));
    }
}
