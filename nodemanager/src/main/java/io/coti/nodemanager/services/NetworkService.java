package io.coti.nodemanager.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.BaseNodeNetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class NetworkService extends BaseNodeNetworkService {

    public Map<String, List<String>> getNetworkSummary() {
        Map<String, List<String>> summaryMap = new HashMap<>();
        createSummaryStringFromNodeList(getMapFromFactory(NodeType.FullNode), summaryMap);
        createSummaryStringFromNodeList(getMapFromFactory(NodeType.DspNode), summaryMap);
        createSummaryStringFromNodeList(getMapFromFactory(NodeType.TrustScoreNode), summaryMap);
        return summaryMap;
    }

    private void createSummaryStringFromNodeList(Map<Hash, NetworkNodeData> networkNodeDataMap, Map<String, List<String>> summaryMap) {
        if (!networkNodeDataMap.isEmpty()) {
            String nodeTypeAsString = networkNodeDataMap.entrySet().iterator().next().getValue().getNodeType().name();
            summaryMap.put(nodeTypeAsString, new LinkedList<String>());
            for (NetworkNodeData nodeData : networkNodeDataMap.values()) {
                summaryMap.get(nodeTypeAsString).add(nodeData.getHttpFullAddress());
            }
        }
    }

    @Override
    public List<Hash> getCurrentValidators() {
        Map<Hash, NetworkNodeData> nodeDataMap = getMapFromFactory(NodeType.DspNode);
        NetworkNodeData zeroSpendNodeData = getSingleNodeData(NodeType.ZeroSpendServer);
        nodeDataMap.put(zeroSpendNodeData.getNodeHash(),zeroSpendNodeData);
        return new ArrayList<>(nodeDataMap.keySet());
    }
}
