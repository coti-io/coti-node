package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;

import java.util.Collection;
import java.util.List;

public interface INetworkService {

    void init();

    void handleNetworkChanges(NetworkData networkData);

    String getRecoveryServerAddress();

    void setRecoveryServerAddress(String recoveryServerAddress);

    void addNode(NetworkNodeData networkNodeData);

    void removeNode(NetworkNodeData networkNodeData);

    boolean updateNetworkNode(NetworkNodeData networkNodeData);

    boolean isNodeExistsOnMemory(NetworkNodeData networkNodeData);

    List<NetworkNodeData> getShuffledNetworkNodeDataListFromMapValues(NodeType nodeType);

    List<NetworkNodeData> getNetworkDataList();

    void addListToSubscriptionAndNetwork(Collection<NetworkNodeData> nodeDataList);

    void setNodeManagerPropagationAddress(String nodeManagerPropagationAddress);

}
