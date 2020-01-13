package io.coti.nodemanager.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.nodemanager.data.NetworkNodeStatus;
import io.coti.nodemanager.data.NodeDayMapData;
import io.coti.nodemanager.data.NodeHistoryData;
import io.coti.nodemanager.data.NodeNetworkDataRecord;
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

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

@Service
public class NetworkHistoryService implements INetworkHistoryService {

    public static final int NUMBER_OF_SECONDS_IN_DAY = 24 * 60 * 60;
    @Autowired
    private NodeHistory nodeHistory;
    @Autowired
    private NodeDayMaps nodeDayMaps;
    @Autowired
    private NodeManagementService nodeManagementService;

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
                LinkedMap<Hash, NodeNetworkDataRecord> nodeHistoryMap = nodeHistoryData.getNodeHistory();
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
            beforePeriodEventRef = nodeHistoryData.getNodeHistory().get(nodeHistoryData.getNodeHistory().firstKey()).getStatusChainRef();
        } else {
            while (localDate != null && !localDate.isAfter(endDate)) {
                NodeHistoryData nodeHistoryData = nodeHistory.getByHash(nodeDayMapData.calculateNodeHistoryDataHash(localDate));
                if (nodeHistoryData != null) {
                    for (LinkedMap.Entry<Hash, NodeNetworkDataRecord> nodeNetworkDataRecordEntry : nodeHistoryData.getNodeHistory().entrySet()) {
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
            if (nodeHistoryData != null && !nodeHistoryData.getNodeHistory().isEmpty()) {
                NodeNetworkResponseData nodeNetworkResponseData = new NodeNetworkResponseData(nodeHistoryData.getNodeHistory().get(beforePeriodEventRef.getRight()));
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
        Hash nodeHash = getNodeStatisticsRequest.getNodeHash();
        LocalDate startDate = getNodeStatisticsRequest.getStartDate();
        LocalDate endDate = getNodeStatisticsRequest.getEndDate();
        if (endDate.isBefore(startDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GetNodeActivityPercentageResponse("Invalid dates range Start:" + startDate + " End:" + endDate, -1));
        }
        LocalDate todayLocalDate = Instant.now().atZone(ZoneId.of("UTC")).toLocalDate();
        endDate = endDate.isAfter(todayLocalDate) ? todayLocalDate : endDate;
        NodeDayMapData nodeDayMapData = nodeDayMaps.getByHash(nodeHash);
        if (nodeDayMapData == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GetNodeActivityPercentageResponse("Invalid node hash " + nodeHash, -1));
        }
        LocalDate firstDateWithEvent = nodeDayMapData.getNodeDaySet().first();
        startDate = startDate.isBefore(firstDateWithEvent) ? firstDateWithEvent : startDate;
        if (endDate.isBefore(startDate)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GetNodeActivityPercentageResponse("Invalid dates range End:" + endDate + " before node started at:" + startDate, -1));
        }

        long activityUpTimeInSeconds = getActivityUpTimeInSeconds(startDate, endDate, nodeDayMapData);
        long numberOfDays = startDate.until(endDate, ChronoUnit.DAYS) + 1;
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GetNodeActivityPercentageResponse(((double)activityUpTimeInSeconds) / (numberOfDays * NUMBER_OF_SECONDS_IN_DAY) * 100));
    }

    private long getActivityUpTimeInSeconds(LocalDate startDate, LocalDate endDate, NodeDayMapData nodeDayMapData) {  // todo check calculation
        long activityUpTimeInSeconds = 0;
        LocalDate ceilingDate = nodeDayMapData.getNodeDaySet().ceiling(endDate);
        LocalDate lastDateWithEvent = ceilingDate == null ? nodeDayMapData.getNodeDaySet().last() : ceilingDate;
        Hash lastDateWithEventHash = nodeDayMapData.calculateNodeHistoryDataHash(lastDateWithEvent);

        NodeHistoryData lastDateWithEventNodeHistoryData = nodeHistory.getByHash(lastDateWithEventHash);
        LinkedMap<Hash, NodeNetworkDataRecord> lastDateWithEventNodeHistory = lastDateWithEventNodeHistoryData.getNodeHistory();
        NodeNetworkDataRecord lastDateWithEventLastEventNodeNetworkDataRecord = lastDateWithEventNodeHistory.get(lastDateWithEventNodeHistory.lastKey());
        if (lastDateWithEventLastEventNodeNetworkDataRecord.getNodeStatus() == NetworkNodeStatus.ACTIVE) {
            activityUpTimeInSeconds += lastDateWithEventLastEventNodeNetworkDataRecord.getRecordTime().until(endDate.plusDays(1).atStartOfDay().atZone(ZoneId.of("UTC")).toInstant(), ChronoUnit.SECONDS);
        }

        NodeNetworkDataRecord previousNodeNetworkDataRecordByChainRef = lastDateWithEventLastEventNodeNetworkDataRecord;
        NodeNetworkDataRecord currentNodeNetworkDataRecordByChainRef = nodeManagementService.getNodeNetworkDataRecordByChainRef(nodeDayMapData, lastDateWithEventLastEventNodeNetworkDataRecord.getStatusChainRef());

        while (!currentNodeNetworkDataRecordByChainRef.getStatusChainRef().getLeft().isBefore(startDate) && previousNodeNetworkDataRecordByChainRef != currentNodeNetworkDataRecordByChainRef) {
            if (previousNodeNetworkDataRecordByChainRef.getNodeStatus() != NetworkNodeStatus.ACTIVE) {
                activityUpTimeInSeconds += currentNodeNetworkDataRecordByChainRef.getRecordTime().until(previousNodeNetworkDataRecordByChainRef.getRecordTime(), ChronoUnit.SECONDS);
            }
            previousNodeNetworkDataRecordByChainRef = currentNodeNetworkDataRecordByChainRef;
            currentNodeNetworkDataRecordByChainRef = nodeManagementService.getNodeNetworkDataRecordByChainRef(nodeDayMapData, previousNodeNetworkDataRecordByChainRef.getStatusChainRef());
        }
        if (currentNodeNetworkDataRecordByChainRef.getStatusChainRef().getLeft().isBefore(startDate) && previousNodeNetworkDataRecordByChainRef.getNodeStatus() != NetworkNodeStatus.ACTIVE) {
            activityUpTimeInSeconds += currentNodeNetworkDataRecordByChainRef.getRecordTime().until(previousNodeNetworkDataRecordByChainRef.getRecordTime(), ChronoUnit.SECONDS);
            activityUpTimeInSeconds += previousNodeNetworkDataRecordByChainRef.getRecordTime().atZone(ZoneId.of("UTC")).toLocalDate().atStartOfDay().until(LocalDateTime.ofInstant(previousNodeNetworkDataRecordByChainRef.getRecordTime(), ZoneOffset.UTC), ChronoUnit.SECONDS);
        }
        return activityUpTimeInSeconds;
    }

}
