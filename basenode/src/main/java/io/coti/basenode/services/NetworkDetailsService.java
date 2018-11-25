package io.coti.basenode.services;

import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.interfaces.INetworkDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class NetworkDetailsService implements INetworkDetailsService {

    private NetworkDetails networkDetails;

    @PostConstruct
    public void init(){
        networkDetails = new NetworkDetails();
    }

    @Override
    public NetworkDetails getNetworkDetails() {
        return networkDetails;
    }

    public void setNetworkDetails(NetworkDetails networkDetails){
        this.networkDetails = networkDetails;
    }

    public void addNode(NetworkNodeData networkNodeData) {
        if (NodeType.ZeroSpendServer.equals(networkNodeData.getNodeType())) {
            networkDetails.setZerospendServer(networkNodeData);
        } else {
            networkDetails.getListFromFactory(networkNodeData.getNodeType()).add(networkNodeData);
        }
    }

    public void removeNode(NetworkNodeData networkNodeData) {
        if (NodeType.ZeroSpendServer.equals(networkNodeData.getNodeType())) {
            networkDetails.setZerospendServer(new NetworkNodeData());
        } else {
            if (!networkDetails.getListFromFactory(networkNodeData.getNodeType()).remove(networkNodeData)) {
                log.info("networkNode {} wasn't found", networkNodeData);
                return;
            }
        }
        log.info("networkNode {} was deleted", networkNodeData);
    }

    public boolean isNodeExistsOnMemory(NetworkNodeData networkNodeData) {
        if (NodeType.ZeroSpendServer.equals(networkNodeData.getNodeType())) {
            if (networkDetails.getZerospendServer() != null && networkDetails.getZerospendServer().equals(networkNodeData)) {
                return true;
            }
        } else {
            if (networkDetails.getListFromFactory(networkNodeData.getNodeType()).contains(networkNodeData)) {
                return true;
            }
        }
        return false;
    }


    public boolean updateNetworkNode(NetworkNodeData networkNodeData) {
        NetworkNodeData node = null;
        if (NodeType.ZeroSpendServer.equals(networkNodeData.getNodeType())) {
            node = networkDetails.getZerospendServer();
        } else {
            List<NetworkNodeData> networkListToChange = networkDetails.getListFromFactory(networkNodeData.getNodeType());
            for (NetworkNodeData iteratedNode : networkListToChange) {
                if (iteratedNode.getHash().equals(networkNodeData.getHash())) {
                    node = iteratedNode;
                }
            }
        }
        if (node != null) {
            node.setAddress(networkNodeData.getAddress());
            node.setHttpPort(networkNodeData.getHttpPort());
            node.setReceivingPort(networkNodeData.getReceivingPort());
            node.setRecoveryServerAddress(networkNodeData.getRecoveryServerAddress());
            node.setPropagationPort(networkNodeData.getPropagationPort());
            node.setSignature(networkNodeData.getSignature());
            node.setSignerHash(networkNodeData.getSignerHash());
            return true;
        }
        return false;
    }



    public Map<String, List<String>> getNetWorkSummary(NetworkDetails networkDetails) {
        Map<String, List<String>> summaryMap = new HashMap<>();
        createSummaryStringFromNodeList(networkDetails.getFullNetworkNodesList(), summaryMap);
        createSummaryStringFromNodeList(networkDetails.getDspNetworkNodesList(), summaryMap);
        createSummaryStringFromNodeList(networkDetails.getTrustScoreNetworkNodesList(), summaryMap);
        if (networkDetails.getZerospendServer() != null) {
            summaryMap.put(networkDetails.getZerospendServer().getNodeType().name(), new LinkedList<>());
            summaryMap.get(networkDetails.getZerospendServer().getNodeType().name()).add(networkDetails.getZerospendServer().getHttpFullAddress());
        }
        return summaryMap;
    }

    private void createSummaryStringFromNodeList(List<NetworkNodeData> networkNodeDataList, Map<String, List<String>> summaryMap) {
        if (!networkNodeDataList.isEmpty()) {
            String nodeTypeAsString = networkNodeDataList.get(0).getNodeType().name();
            summaryMap.put(nodeTypeAsString, new LinkedList<String>());
            for (NetworkNodeData nodeData : networkNodeDataList) {
                summaryMap.get(nodeTypeAsString).add(nodeData.getHttpFullAddress());
            }
        }
    }

}
