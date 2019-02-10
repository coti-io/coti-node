package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface INetworkService {

    void init();

    void handleNetworkChanges(NetworkData networkData);

    String getRecoveryServerAddress();

    void setRecoveryServerAddress(String recoveryServerAddress);

    String getNodeManagerPropagationAddress();

    Map<Hash, NetworkNodeData> getMapFromFactory(NodeType nodeType);

    NetworkNodeData getSingleNodeData(NodeType nodeType);

    void addNode(NetworkNodeData networkNodeData);

    void removeNode(NetworkNodeData networkNodeData);

    boolean updateNetworkNode(NetworkNodeData networkNodeData);

    void validateNetworkNodeData(NetworkNodeData networkNodeData) throws Exception;

    boolean isNodeExistsOnMemory(NetworkNodeData networkNodeData);

    List<NetworkNodeData> getShuffledNetworkNodeDataListFromMapValues(NodeType nodeType);

    List<NetworkNodeData> getNetworkNodeDataList();

    void addListToSubscriptionAndNetwork(Collection<NetworkNodeData> nodeDataList);

    void setNodeManagerPropagationAddress(String nodeManagerPropagationAddress);

    NetworkData getNetworkData();

    void setNetworkData(NetworkData networkData);

}
