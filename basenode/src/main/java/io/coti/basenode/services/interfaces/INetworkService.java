package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface INetworkService {

    void init();

    void handleNetworkChanges(NetworkData networkData);

    String getRecoveryServerAddress();

    void setRecoveryServerAddress(String recoveryServerAddress);

    Map<Hash, NetworkNodeData> getMapFromFactory(NodeType nodeType);

    NetworkNodeData getSingleNodeData(NodeType nodeType);

    void addNode(NetworkNodeData networkNodeData);

    void removeNode(NetworkNodeData networkNodeData);

    void validateNetworkNodeData(NetworkNodeData networkNodeData);

    String getProtocol(String webServerUrl);

    boolean validateFeeData(FeeData feeData);

    List<NetworkNodeData> getShuffledNetworkNodeDataListFromMapValues(NodeType nodeType);

    List<NetworkNodeData> getNetworkNodeDataList();

    void addListToSubscription(Collection<NetworkNodeData> nodeDataList);

    void handleConnectedDspNodesChange(List<NetworkNodeData> connectedDspNodes, Map<Hash, NetworkNodeData> newDspNodeMap, NodeType nodeType);

    void handleConnectedSingleNodeChange(NetworkData newNetworkData, NodeType singleNodeType, NodeType connectingNodeType);

    void setNodeManagerPropagationAddress(String nodeManagerPropagationAddress);

    String getNodeManagerPropagationAddress();

    void setConnectToNetworkUrl(String connectToNetworkUrl);

    NetworkData getNetworkData();

    void setNetworkData(NetworkData networkData);

    void setNetworkNodeData(NetworkNodeData networkNodeData);

    NetworkNodeData getNetworkNodeData();

    void connectToNetwork();

    String getHost(String webServerUrl);

}
