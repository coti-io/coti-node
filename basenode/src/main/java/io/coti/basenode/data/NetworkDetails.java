package io.coti.basenode.data;

import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.omg.CORBA.portable.IDLEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Slf4j
public class NetworkDetails implements IEntity {
    private List<NetworkNodeData> dspNetworkNodesList;
    private List<NetworkNodeData> fullNetworkNodesList;
    private List<NetworkNodeData> trustScoreNetworkNodesList;
    private String nodeManagerPropagationAddress;
    private NetworkNodeData zerospendServer;


    public NetworkDetails() {
        dspNetworkNodesList = Collections.synchronizedList(new ArrayList<>());
        fullNetworkNodesList = Collections.synchronizedList(new ArrayList<>());
        trustScoreNetworkNodesList = Collections.synchronizedList(new ArrayList<>());
    }

    public void addNode(NetworkNodeData networkNodeData) {
        if (NodeType.ZeroSpendServer.equals(networkNodeData.getNodeType())) {
            zerospendServer = networkNodeData;
        } else {
            getListFromFactory(networkNodeData.getNodeType()).add(networkNodeData);
        }
    }

    public void removeNode(NetworkNodeData networkNodeData) {
        if (NodeType.ZeroSpendServer.equals(networkNodeData.getNodeType())) {
            zerospendServer = new NetworkNodeData();
        } else {
            if (!getListFromFactory(networkNodeData.getNodeType()).remove(networkNodeData)) {
                log.info("networkNode {} wasn't found", networkNodeData);
                return;
            }
        }
        log.info("networkNode {} was deleted", networkNodeData);
    }

    public boolean nodeExists(NetworkNodeData networkNodeData) {
        if (NodeType.ZeroSpendServer.equals(networkNodeData.getNodeType())) {
            if (zerospendServer != null && zerospendServer.equals(networkNodeData)) {
                return true;
            }
        } else {
            if (getListFromFactory(networkNodeData.getNodeType()).contains(networkNodeData)) {
                return true;
            }
        }
        return false;
    }

    private List<NetworkNodeData> getListFromFactory(NodeType nodeType) {
        switch (nodeType) {
            case DspNode:
                return dspNetworkNodesList;
            case FullNode:
                return fullNetworkNodesList;
            case TrustScoreNode:
                return trustScoreNetworkNodesList;
            default:
                log.error("Unsupported networkNodeData type ( {} ) is not deleted", nodeType);
        }
        return Collections.emptyList();
    }

    public void updateNetworkNode(NetworkNodeData networkNodeData) {
        NetworkNodeData node = null;
        if (NodeType.ZeroSpendServer.equals(networkNodeData.getNodeType())) {
            node = zerospendServer;
        } else {
            List<NetworkNodeData> networkListToChange = getListFromFactory(networkNodeData.getNodeType());
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
        }
    }


    public String getNetWorkSummaryString(){
        String ans = "";
        ans += createSummaryStringFromNodeList(fullNetworkNodesList);
        ans += createSummaryStringFromNodeList(dspNetworkNodesList);
        ans += createSummaryStringFromNodeList(trustScoreNetworkNodesList);
        if(zerospendServer != null) {
            ans += zerospendServer.getNodeType() + ": " + System.lineSeparator();
            ans += zerospendServer.getHttpFullAddress();
        }
        return  ans;
    }

    private String createSummaryStringFromNodeList(List<NetworkNodeData> networkNodeDataList){
        String ans = "";
        if(!networkNodeDataList.isEmpty()) {
            ans += networkNodeDataList.get(0).getNodeType() + ", num of nodes(" + networkNodeDataList.size() +"):"
                    + System.lineSeparator();

            for (NetworkNodeData nodeData : networkNodeDataList) {
                ans += nodeData.getHttpFullAddress() + System.lineSeparator();
            }

        }
        return ans;



    }


    @Override
    public Hash getHash() {
        return new Hash(1);
    }

    @Override
    public void setHash(Hash hash) {

    }
}