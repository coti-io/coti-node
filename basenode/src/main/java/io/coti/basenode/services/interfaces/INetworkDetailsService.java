package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;

import java.util.List;
import java.util.Map;

public interface INetworkDetailsService {


    void addNode(NetworkNodeData networkNodeData);

    void removeNode(NetworkNodeData networkNodeData);

    boolean isNodeExistsOnMemory(NetworkNodeData networkNodeData);


    boolean updateNetworkNode(NetworkNodeData networkNodeData);


    Map<String, List<String>> getNetWorkSummary(NetworkDetails networkDetails);

    void init();

    NetworkDetails getNetworkDetails();

    void setNetworkDetails(NetworkDetails networkDetails);

    List<NetworkNodeData> getShuffledNetworkNodeDataListFromMapValues(Map<Hash, NetworkNodeData> dataMap);

}
