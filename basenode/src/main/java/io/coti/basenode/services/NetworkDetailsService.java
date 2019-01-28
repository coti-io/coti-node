package io.coti.basenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.interfaces.INetworkDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
@Slf4j
public class NetworkDetailsService implements INetworkDetailsService {

    private NetworkData networkData;

    @PostConstruct
    public void init() {
        networkData = new NetworkData();
    }

    @Override
    public NetworkData getNetworkData() {
        return networkData;
    }

    public void setNetworkData(NetworkData networkData) {
        this.networkData = networkData;
    }

    @Override
    public List<NetworkNodeData> getShuffledNetworkNodeDataListFromMapValues(Map<Hash, NetworkNodeData> dataMap) {
        List<NetworkNodeData> nodeDataList = new LinkedList<>(dataMap.values());
        Collections.shuffle(nodeDataList);
        return nodeDataList;
    }

    public void addNode(NetworkNodeData networkNodeData) {
        if (NodeType.ZeroSpendServer.equals(networkNodeData.getNodeType())) {
            networkData.setZerospendServer(networkNodeData);
        } else {
            networkData.getMapFromFactory(networkNodeData.getNodeType()).put(networkNodeData.getHash(), networkNodeData);
        }
    }

    public void removeNode(NetworkNodeData networkNodeData) {
        if (NodeType.ZeroSpendServer.equals(networkNodeData.getNodeType())) {
            networkData.setZerospendServer(new NetworkNodeData());
        } else {
            if (networkData.getMapFromFactory(networkNodeData.getNodeType()).remove(networkNodeData.getHash()) == null) {
                log.info("networkNode {} wasn't found", networkNodeData);
                return;
            }
        }
        log.info("networkNode {} was deleted", networkNodeData);
    }

    public boolean isNodeExistsOnMemory(NetworkNodeData networkNodeData) {
        if (NodeType.ZeroSpendServer.equals(networkNodeData.getNodeType())) {
            if (networkData.getZerospendServer() != null && networkData.getZerospendServer().equals(networkNodeData)) {
                return true;
            }
        } else {
            if (networkData.getMapFromFactory(networkNodeData.getNodeType()).containsKey(networkNodeData.getHash())) {
                return true;
            }
        }
        return false;
    }


    public boolean updateNetworkNode(NetworkNodeData networkNodeData) {
        NetworkNodeData node = null;
        if (NodeType.ZeroSpendServer.equals(networkNodeData.getNodeType())) {
            node = networkData.getZerospendServer();
        } else {
            Map<Hash, NetworkNodeData> networkMapToChange = networkData.getMapFromFactory(networkNodeData.getNodeType());
            for (NetworkNodeData iteratedNode : networkMapToChange.values()) {
                if (iteratedNode.getHash().equals(networkNodeData.getHash())) {
                    node = iteratedNode;
                }
            }
        }
        return false;
    }


    public Map<String, List<String>> getNetworkSummary(NetworkData networkData) {
        Map<String, List<String>> summaryMap = new HashMap<>();
        createSummaryStringFromNodeList(networkData.getFullNodeNetworkNodesMap(), summaryMap);
        createSummaryStringFromNodeList(networkData.getDspNetworkNodesMap(), summaryMap);
        createSummaryStringFromNodeList(networkData.getTrustScoreNetworkNodesMap(), summaryMap);
        if (networkData.getZerospendServer() != null) {
            summaryMap.put(networkData.getZerospendServer().getNodeType().name(), new LinkedList<>());
            summaryMap.get(networkData.getZerospendServer().getNodeType().name()).add(networkData.getZerospendServer().getHttpFullAddress());
        }
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

}
