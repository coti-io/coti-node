package io.coti.nodemanager.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.Response;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.nodemanager.data.*;
import io.coti.nodemanager.exceptions.NetworkHistoryValidationException;
import io.coti.nodemanager.http.GetNodeActivityInSecondsResponse;
import io.coti.nodemanager.http.GetNodeActivityPercentageResponse;
import io.coti.nodemanager.http.GetNodeStatisticsRequest;
import io.coti.nodemanager.http.data.NodeDailyStatisticsData;
import io.coti.nodemanager.http.data.NodeNetworkResponseData;
import io.coti.nodemanager.http.data.NodeStatisticsData;
import io.coti.nodemanager.model.NodeDayMaps;
import io.coti.nodemanager.model.NodeHistory;
import io.coti.nodemanager.services.interfaces.INetworkHistoryService;
import org.apache.commons.collections4.map.LinkedMap;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;

import static io.coti.basenode.http.BaseNodeHttpStringConstants.STATUS_ERROR;

@Service
public class NetworkHistoryService implements INetworkHistoryService {

    public static final int NUMBER_OF_SECONDS_IN_DAY = 24 * 60 * 60;
    @Autowired
    private NodeHistory nodeHistory;
    @Autowired
    private NodeDayMaps nodeDayMaps;

    @Override
    public List<NodeHistoryData> getNodesHistory() {
        List<NodeHistoryData> nodeHistoryDataList = new LinkedList<>();
        nodeHistory.forEach(nodeHistoryDataList::add);
        return nodeHistoryDataList;
    }

    @Override
    public LinkedList<NodeNetworkResponseData> getNodeEvents(GetNodeStatisticsRequest getNodeStatisticsRequest) {
        Hash nodeHash = getNodeStatisticsRequest.getNodeHash();
        LocalDate startDate = getNodeStatisticsRequest.getStartDate();
        LocalDate endDate = getNodeStatisticsRequest.getEndDate();

        LinkedList<NodeNetworkResponseData> nodeEvents = new LinkedList<>();
        NodeDayMapData nodeDayMapData = nodeDayMaps.getByHash(nodeHash);
        if (nodeDayMapData == null) {
            return nodeEvents;
        }
        Pair<LocalDate, Hash> beforePeriodEventRef = null;
        ConcurrentSkipListSet<LocalDate> nodeDaySet = nodeDayMapData.getNodeDaySet();
        LocalDate localDate = nodeDaySet.ceiling(startDate);
        if (localDate == null) {
            LocalDate prevDate = nodeDaySet.floor(startDate);
            while (beforePeriodEventRef == null) {
                NodeHistoryData nodeHistoryData = nodeHistory.getByHash(nodeDayMapData.calculateNodeHistoryDataHash(prevDate));
                LinkedMap<Hash, NodeNetworkDataRecord> nodeHistoryMap = nodeHistoryData.getNodeNetworkDataRecordMap();
                if (nodeHistoryData != null && !nodeHistoryMap.isEmpty()) {
                    Hash nodeNetworkDataRecordHash = nodeHistoryMap.lastKey();
                    while (beforePeriodEventRef == null && nodeNetworkDataRecordHash != null) {
                        NodeNetworkDataRecord nodeNetworkDataRecord = nodeHistoryMap.get(nodeNetworkDataRecordHash);
                        if (nodeNetworkDataRecord.getNodeStatus() == NetworkNodeStatus.ACTIVE) {
                            beforePeriodEventRef = nodeNetworkDataRecord.getStatusChainRef();
                        } else if (nodeNetworkDataRecord.getNodeStatus() == NetworkNodeStatus.INACTIVE) {
                            return nodeEvents;
                        }
                        nodeNetworkDataRecordHash = nodeHistoryMap.previousKey(nodeNetworkDataRecordHash);
                    }
                }
                prevDate = nodeDaySet.lower(prevDate);
                if (prevDate == null) {
                    break;
                }
            }
        } else if (localDate.isAfter(endDate)) {
            NodeHistoryData nodeHistoryData = nodeHistory.getByHash(nodeDayMapData.calculateNodeHistoryDataHash(localDate));
            beforePeriodEventRef = nodeHistoryData.getNodeNetworkDataRecordMap().get(nodeHistoryData.getNodeNetworkDataRecordMap().firstKey()).getStatusChainRef();
        } else {
            while (localDate != null && !localDate.isAfter(endDate)) {
                NodeHistoryData nodeHistoryData = nodeHistory.getByHash(nodeDayMapData.calculateNodeHistoryDataHash(localDate));
                if (nodeHistoryData != null) {
                    for (LinkedMap.Entry<Hash, NodeNetworkDataRecord> nodeNetworkDataRecordEntry : nodeHistoryData.getNodeNetworkDataRecordMap().entrySet()) {
                        if (nodeNetworkDataRecordEntry.getValue().getNodeStatus() == NetworkNodeStatus.ACTIVE
                                || nodeNetworkDataRecordEntry.getValue().getNodeStatus() == NetworkNodeStatus.INACTIVE) {
                            nodeEvents.add(new NodeNetworkResponseData(nodeNetworkDataRecordEntry.getValue()));
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
            Hash prevDateHashEntryHash = nodeDayMapData.calculateNodeHistoryDataHash(beforePeriodEventRef.getLeft());
            NodeHistoryData nodeHistoryData = nodeHistory.getByHash(prevDateHashEntryHash);
            if (nodeHistoryData != null && !nodeHistoryData.getNodeNetworkDataRecordMap().isEmpty()) {
                NodeNetworkResponseData nodeNetworkResponseData = new NodeNetworkResponseData(nodeHistoryData.getNodeNetworkDataRecordMap().get(beforePeriodEventRef.getRight()));
                nodeEvents.addFirst(nodeNetworkResponseData);
            }
        }
        return nodeEvents;
    }

    @Override
    public List<NodeDailyStatisticsData> getNodeDailyStats(GetNodeStatisticsRequest getNodeStatisticsRequest) {
        LocalDate startDate = getNodeStatisticsRequest.getStartDate();
        LocalDate endDate = getNodeStatisticsRequest.getEndDate();

        LinkedList<NodeNetworkResponseData> eventsList = getNodeEvents(getNodeStatisticsRequest);
        List<NodeDailyStatisticsData> nodeStatisticsList = new LinkedList<>();
        if (eventsList == null || eventsList.isEmpty()) {
            return nodeStatisticsList;
        }

        NetworkNodeStatus currentNetworkNodeStatus = NetworkNodeStatus.INACTIVE;
        if (eventsList.getFirst().getRecordDateTime().atZone(ZoneId.of("UTC")).toLocalDate().isBefore(startDate)) {
            currentNetworkNodeStatus = eventsList.getFirst().getNodeStatus();
        }
        int eventsListIndex = 0;

        for (LocalDate localDate = startDate; !localDate.isAfter(endDate); localDate = localDate.plusDays(1)) {
            long upTime = 0;
            int restarts = 0;
            int downEvents = 0;
            Instant lastActive = localDate.atStartOfDay().atZone(ZoneId.of("UTC")).toInstant();

            while (eventsListIndex < eventsList.size() && eventsList.get(eventsListIndex) != null
                    && eventsList.get(eventsListIndex).getRecordDateTime().atZone(ZoneId.of("UTC")).toLocalDate().isBefore(localDate)) {
                eventsListIndex += 1;
            }
            while (eventsListIndex < eventsList.size()
                    && eventsList.get(eventsListIndex).getRecordDateTime().atZone(ZoneId.of("UTC")).toLocalDate().isEqual(localDate)) {
                NodeNetworkResponseData nodeNetworkResponseData = eventsList.get(eventsListIndex);
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
                upTime += lastActive.until(localDate.plusDays(1).atStartOfDay().atZone(ZoneId.of("UTC")).toInstant(), ChronoUnit.SECONDS);
            }

            NodeDailyStatisticsData nodeDailyStatisticsData = new NodeDailyStatisticsData(localDate, upTime, restarts, downEvents);
            nodeStatisticsList.add(nodeDailyStatisticsData);
        }
        return nodeStatisticsList;
    }

    @Override
    public NodeStatisticsData getNodeStatsTotal(GetNodeStatisticsRequest getNodeStatisticsRequest) {
        LocalDate startDate = getNodeStatisticsRequest.getStartDate();
        LocalDate endDate = getNodeStatisticsRequest.getEndDate();

        LinkedList<NodeNetworkResponseData> eventsList = getNodeEvents(getNodeStatisticsRequest);
        if (eventsList == null || eventsList.isEmpty()) {
            return new NodeStatisticsData(0, 0, 0);
        }

        NetworkNodeStatus currentNetworkNodeStatus = NetworkNodeStatus.INACTIVE;
        if (eventsList.getFirst().getRecordDateTime().atZone(ZoneId.of("UTC")).toLocalDate().isBefore(startDate)) {
            currentNetworkNodeStatus = eventsList.getFirst().getNodeStatus();
        }

        long upTime = 0;
        int restarts = 0;
        int downEvents = 0;
        Instant lastActive = startDate.atStartOfDay().atZone(ZoneId.of("UTC")).toInstant();

        for (NodeNetworkResponseData nodeNetworkResponseData : eventsList) {
            if (!nodeNetworkResponseData.getRecordDateTime().isBefore(startDate.atStartOfDay().atZone(ZoneId.of("UTC")).toInstant())) {
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
            }
        }
        if (currentNetworkNodeStatus == NetworkNodeStatus.ACTIVE) {
            upTime += lastActive.until(endDate.plusDays(1).atStartOfDay().atZone(ZoneId.of("UTC")).toInstant(), ChronoUnit.SECONDS);
        }

        return new NodeStatisticsData(upTime, restarts, downEvents);
    }

    @Override
    public ResponseEntity<IResponse> getNodeActivityPercentage(GetNodeStatisticsRequest getNodeStatisticsRequest) {
        try {
            NodeActivityData nodeActivityData = getNodeActivity(getNodeStatisticsRequest);

            long activityUpTimeInSeconds = nodeActivityData.getActivityUpTimeInSeconds();
            long numberOfDays = nodeActivityData.getNumberOfDays();
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new GetNodeActivityPercentageResponse(((double) activityUpTimeInSeconds) / (numberOfDays * NUMBER_OF_SECONDS_IN_DAY) * 100));
        } catch (NetworkHistoryValidationException e) {
            return ResponseEntity.badRequest().body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    @Override
    public ResponseEntity<IResponse> getNodeActivityInSeconds(GetNodeStatisticsRequest getNodeStatisticsRequest) {
        try {
            NodeActivityData nodeActivityData = getNodeActivity(getNodeStatisticsRequest);
            return ResponseEntity.ok()
                    .body(new GetNodeActivityInSecondsResponse(nodeActivityData));
        } catch (NetworkHistoryValidationException e) {
            return ResponseEntity.badRequest().body(new Response(e.getMessage(), STATUS_ERROR));
        }
    }

    @Override
    public NodeActivityData getNodeActivity(GetNodeStatisticsRequest getNodeStatisticsRequest) {
        Hash nodeHash = getNodeStatisticsRequest.getNodeHash();
        LocalDate startDate = getNodeStatisticsRequest.getStartDate();
        LocalDate endDate = getNodeStatisticsRequest.getEndDate();
        if (endDate.isBefore(startDate)) {
            throw new NetworkHistoryValidationException("Invalid dates range Start: " + startDate + " End: " + endDate);
        }
        LocalDate todayLocalDate = Instant.now().atZone(ZoneId.of("UTC")).toLocalDate();
        endDate = endDate.isAfter(todayLocalDate) ? todayLocalDate : endDate;
        NodeDayMapData nodeDayMapData = nodeDayMaps.getByHash(nodeHash);
        if (nodeDayMapData == null) {
            throw new NetworkHistoryValidationException("Invalid node hash " + nodeHash);
        }
        LocalDate firstDateWithEvent = nodeDayMapData.getNodeDaySet().first();
        startDate = startDate.isBefore(firstDateWithEvent) ? firstDateWithEvent : startDate;
        if (endDate.isBefore(startDate)) {
            throw new NetworkHistoryValidationException("Invalid dates range End: " + endDate + " before node started at: " + startDate);
        }

        long activityUpTimeInSeconds = getActivityUpTimeInSeconds(startDate, endDate, nodeDayMapData);
        long numberOfDays = startDate.until(endDate, ChronoUnit.DAYS) + 1;
        return new NodeActivityData(activityUpTimeInSeconds, numberOfDays);
    }

    private long getActivityUpTimeInSeconds(LocalDate startDate, LocalDate endDate, NodeDayMapData nodeDayMapData) {
        long activityUpTimeInSeconds = 0;
        LocalDate ceilingDate = nodeDayMapData.getNodeDaySet().ceiling(endDate);
        LocalDate lastDateWithEvent = Optional.ofNullable(ceilingDate).orElse(nodeDayMapData.getNodeDaySet().last());
        Hash lastDateWithEventHash = calculateNodeHistoryDataHash(nodeDayMapData.getNodeHash(), lastDateWithEvent);
        NodeHistoryData lastDateWithEventNodeHistoryData = nodeHistory.getByHash(lastDateWithEventHash);
        LinkedMap<Hash, NodeNetworkDataRecord> lastDateWithEventNodeHistory = lastDateWithEventNodeHistoryData.getNodeNetworkDataRecordMap();
        NodeNetworkDataRecord lastRelevantNodeNetworkDataRecord;
        Instant endInstant = null;
        Instant startInstant = localDateToInstant(startDate);
        if (!lastDateWithEvent.isAfter(endDate)) {
            lastRelevantNodeNetworkDataRecord = lastDateWithEventNodeHistory.get(lastDateWithEventNodeHistory.lastKey());
            if (!lastRelevantNodeNetworkDataRecord.getNodeStatus().equals(NetworkNodeStatus.ACTIVE)) {
                endInstant = lastRelevantNodeNetworkDataRecord.getRecordTime();
            }
        } else {
            lastRelevantNodeNetworkDataRecord = lastDateWithEventNodeHistory.get(lastDateWithEventNodeHistory.firstKey());
            endInstant = localDateToInstant(endDate.plusDays(1));
        }
        NodeNetworkDataRecord nodeNetworkDataRecordByChainRef = getNodeNetworkDataRecordByChainRef(lastRelevantNodeNetworkDataRecord);
        if (nodeNetworkDataRecordByChainRef.getRecordTime().isAfter(startInstant)) {
            startInstant = nodeNetworkDataRecordByChainRef.getRecordTime();
        }
        if (nodeNetworkDataRecordByChainRef.getNodeStatus().equals(NetworkNodeStatus.ACTIVE)) {
            activityUpTimeInSeconds += startInstant.until(endInstant, ChronoUnit.SECONDS);
            lastRelevantNodeNetworkDataRecord = getNodeNetworkDataRecordByChainRef(nodeNetworkDataRecordByChainRef);
        } else {
            lastRelevantNodeNetworkDataRecord = nodeNetworkDataRecordByChainRef;
        }

        while (startInstant != localDateToInstant(startDate) && lastRelevantNodeNetworkDataRecord != null) {
            endInstant = lastRelevantNodeNetworkDataRecord.getRecordTime();
            nodeNetworkDataRecordByChainRef = getNodeNetworkDataRecordByChainRef(lastRelevantNodeNetworkDataRecord);
            if (nodeNetworkDataRecordByChainRef.getRecordTime().isAfter(startInstant)) {
                startInstant = nodeNetworkDataRecordByChainRef.getRecordTime();
            } else {
                startInstant = localDateToInstant(startDate);
            }
            activityUpTimeInSeconds += startInstant.until(endInstant, ChronoUnit.SECONDS);
            lastRelevantNodeNetworkDataRecord = getNodeNetworkDataRecordByChainRef(nodeNetworkDataRecordByChainRef);
        }
        return activityUpTimeInSeconds;
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
