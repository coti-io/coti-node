package io.coti.nodemanager.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.nodemanager.data.*;
import io.coti.nodemanager.http.GetNodeDetailsRequest;
import io.coti.nodemanager.http.GetNodeStatisticsRequest;
import io.coti.nodemanager.http.GetNodesActivityPercentageRequest;
import org.apache.commons.collections4.map.LinkedMap;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public interface INetworkHistoryService {

    List<NodeHistoryData> getNodesHistory();

    ResponseEntity<IResponse> getNodeEventsResponse(GetNodeStatisticsRequest getNodeStatisticsRequest);

    ResponseEntity<IResponse> getNodeDailyStats(GetNodeStatisticsRequest getNodeStatisticsRequest);

    ResponseEntity<IResponse> getNodeStatsTotal(GetNodeStatisticsRequest getNodeStatisticsRequest);

    ResponseEntity<IResponse> getNodesActivityPercentage(GetNodesActivityPercentageRequest getNodesActivityPercentageRequest);

    ResponseEntity<IResponse> getNodeActivityPercentage(GetNodeStatisticsRequest getNodeStatisticsRequest);

    ResponseEntity<IResponse> getNodeActivityInSeconds(GetNodeStatisticsRequest getNodeStatisticsRequest);

    NodeActivityData getNodeActivity(GetNodeStatisticsRequest getNodeStatisticsRequest, Instant now);

    ResponseEntity<IResponse> getNodeActivityInSecondsByDay(GetNodeStatisticsRequest getNodeStatisticsRequest);

    ResponseEntity<IResponse> getNodeActivationTime(GetNodeDetailsRequest getNodeDetailsRequest);

    NodeNetworkDataRecord getNodeNetworkDataRecordByChainRef(NodeNetworkDataRecord nodeNetworkDataRecord);

    Hash calculateNodeHistoryDataHash(Hash nodeHash, LocalDate localDate);

    NodeNetworkDataRecord getReferenceNodeNetworkDataRecordByStatus(NodeNetworkDataRecord nodeNetworkDataRecord, LinkedMap<Hash, NodeNetworkDataRecord> nodeNetworkDataRecordMap, NetworkNodeStatus networkNodeStatus);

    Pair<LocalDate, Hash> getReferenceToRecord(NodeNetworkDataRecord nodeNetworkDataRecord);

    ResponseEntity<IResponse> getNodeLastEvent(GetNodeDetailsRequest getNodeDetailsRequest);

    NodeNetworkDataRecord getLastNodeNetworkDataRecord(Hash nodeHash);

    NodeNetworkDataRecord getLastNodeNetworkDataRecord(Hash nodeHash, NodeDailyActivityData nodeDailyActivityData);

    NodeNetworkDataRecord getLastNodeNetworkDataRecord(NodeHistoryData nodeHistoryData);

    NodeNetworkDataRecord getFirstNodeNetworkDataRecord(NodeHistoryData nodeHistoryData);

    NodeHistoryData getNodeHistoryData(Hash nodeHash, LocalDate localDate);
}
