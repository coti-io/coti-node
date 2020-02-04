package io.coti.nodemanager.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.nodemanager.data.NodeActivityData;
import io.coti.nodemanager.data.NodeHistoryData;
import io.coti.nodemanager.data.NodeNetworkDataRecord;
import io.coti.nodemanager.http.GetNodeActivationTimeRequest;
import io.coti.nodemanager.http.GetNodeStatisticsRequest;
import io.coti.nodemanager.http.data.NodeDailyStatisticsData;
import io.coti.nodemanager.http.data.NodeStatisticsData;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

public interface INetworkHistoryService {

    List<NodeHistoryData> getNodesHistory();

    LinkedList<Object> getNodeEvents(GetNodeStatisticsRequest getNodeStatisticsRequest, boolean rawData);

    List<NodeDailyStatisticsData> getNodeDailyStats(GetNodeStatisticsRequest getNodeStatisticsRequest);

    NodeStatisticsData getNodeStatsTotal(GetNodeStatisticsRequest getNodeStatisticsRequest);

    ResponseEntity<IResponse> getNodeActivityPercentage(GetNodeStatisticsRequest getNodeStatisticsRequest);

    ResponseEntity<IResponse> getNodeActivityInSeconds(GetNodeStatisticsRequest getNodeStatisticsRequest);

    NodeActivityData getNodeActivity(GetNodeStatisticsRequest getNodeStatisticsRequest, Instant now);

    ResponseEntity<IResponse> getNodeActivityInSecondsByDay(GetNodeStatisticsRequest getNodeStatisticsRequest);

    ResponseEntity<IResponse> getNodeActivationTime(GetNodeActivationTimeRequest getNodeActivationTimeRequest);

    Instant getFirstNodeNetworkDataRecordTime(Hash nodeHash);
}
