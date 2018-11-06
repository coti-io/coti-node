package io.coti.nodemanager.services;

import io.coti.nodemanager.data.NodeHistoryData;
import io.coti.nodemanager.model.NodeHistory;
import io.coti.nodemanager.services.interfaces.INetworkHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class NetworkHistoryService implements INetworkHistoryService {

    @Autowired
    private NodeHistory nodeHistory;

    @Override
    public List<NodeHistoryData> getNodesHistory() {
        List<NodeHistoryData> nodeHistoryDataList = new LinkedList<>();
        nodeHistory.forEach(nodeHistoryData -> nodeHistoryDataList.add(nodeHistoryData));
        return nodeHistoryDataList;
    }
}
