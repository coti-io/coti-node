package io.coti.nodemanager.services.interfaces;

import io.coti.nodemanager.data.NodeHistoryData;
import io.coti.nodemanager.http.GetNodeStatisticsRequest;
import io.coti.nodemanager.http.data.NodeDailyStatisticsData;
import io.coti.nodemanager.http.data.NodeNetworkResponseData;
import io.coti.nodemanager.http.data.NodeStatisticsData;

import java.util.LinkedList;
import java.util.List;

public interface INetworkHistoryService {

    List<NodeHistoryData> getNodesHistory();

    LinkedList<NodeNetworkResponseData> getNodeEvents(GetNodeStatisticsRequest getNodeStatisticsRequest);

    List<NodeDailyStatisticsData> getNodeDailyStats(GetNodeStatisticsRequest getNodeStatisticsRequest);

    NodeStatisticsData getNodeStatsTotal(GetNodeStatisticsRequest getNodeStatisticsRequest);
}
