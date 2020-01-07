package io.coti.nodemanager.services;

import io.coti.basenode.data.Hash;
import io.coti.nodemanager.data.NetworkNodeStatus;
import io.coti.nodemanager.data.NodeDayMapData;
import io.coti.nodemanager.data.NodeHistoryData;
import io.coti.nodemanager.data.NodeNetworkDataRecord;
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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

@Service
public class NetworkHistoryService implements INetworkHistoryService {

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
        ConcurrentSkipListMap<LocalDate, Hash> nodeDayMap = nodeDayMapData.getNodeDayMap();
        ConcurrentSkipListMap.Entry<LocalDate, Hash> localDateHashEntry = nodeDayMap.ceilingEntry(startDate);
        if (localDateHashEntry == null) {
            ConcurrentSkipListMap.Entry<LocalDate, Hash> prevDateRecord = nodeDayMap.floorEntry(startDate);
            while (beforePeriodEventRef == null) {
                NodeHistoryData nodeHistoryData = nodeHistory.getByHash(prevDateRecord.getValue());
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
                prevDateRecord = nodeDayMap.lowerEntry(prevDateRecord.getKey());
                if (prevDateRecord == null) {
                    break;
                }
            }
        } else if (localDateHashEntry.getKey().isAfter(endDate)) {
            NodeHistoryData nodeHistoryData = nodeHistory.getByHash(localDateHashEntry.getValue());
            beforePeriodEventRef = nodeHistoryData.getNodeHistory().get(nodeHistoryData.getNodeHistory().firstKey()).getStatusChainRef();
        } else {
            while (localDateHashEntry != null && !localDateHashEntry.getKey().isAfter(endDate)) {
                NodeHistoryData nodeHistoryData = nodeHistory.getByHash(localDateHashEntry.getValue());
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
                localDateHashEntry = nodeDayMap.higherEntry(localDateHashEntry.getKey());
            }
        }

        if (beforePeriodEventRef != null && beforePeriodEventRef.getLeft().isBefore(startDate)) {
            Hash prevDateHashEntryHash = nodeDayMap.get(beforePeriodEventRef.getLeft());
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
        if (eventsList.getFirst().getRecordDateTime().toLocalDate().isBefore(startDate)) {
            currentNetworkNodeStatus = eventsList.getFirst().getNodeStatus();
        }
        int eventsListIndex = 0;

        for (LocalDate localDate = startDate; !localDate.isAfter(endDate); localDate = localDate.plusDays(1)) {
            long upTime = 0;
            int restarts = 0;
            int downEvents = 0;
            LocalDateTime lastActive = localDate.atStartOfDay();

            while (eventsListIndex < eventsList.size() && eventsList.get(eventsListIndex) != null && eventsList.get(eventsListIndex).getRecordDateTime().toLocalDate().isBefore(localDate)) {
                eventsListIndex += 1;
            }
            while (eventsListIndex < eventsList.size() && eventsList.get(eventsListIndex).getRecordDateTime().toLocalDate().isEqual(localDate)) {
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
                upTime += lastActive.until(localDate.plusDays(1).atStartOfDay(), ChronoUnit.SECONDS);
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
        if (eventsList.getFirst().getRecordDateTime().toLocalDate().isBefore(startDate)) {
            currentNetworkNodeStatus = eventsList.getFirst().getNodeStatus();
        }

        long upTime = 0;
        int restarts = 0;
        int downEvents = 0;
        LocalDateTime lastActive = startDate.atStartOfDay();

        for (NodeNetworkResponseData nodeNetworkResponseData : eventsList) {
            if (!nodeNetworkResponseData.getRecordDateTime().isBefore(startDate.atStartOfDay())) {
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
            upTime += lastActive.until(endDate.plusDays(1).atStartOfDay(), ChronoUnit.SECONDS);
        }

        return new NodeStatisticsData(upTime, restarts, downEvents);
    }

}
