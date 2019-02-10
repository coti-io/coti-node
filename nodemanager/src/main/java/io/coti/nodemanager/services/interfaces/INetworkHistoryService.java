package io.coti.nodemanager.services.interfaces;

import io.coti.nodemanager.data.NodeHistoryData;

import java.util.List;

public interface INetworkHistoryService {

    List<NodeHistoryData> getNodesHistory();
}
