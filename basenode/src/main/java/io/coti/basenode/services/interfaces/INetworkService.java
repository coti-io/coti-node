package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.http.GetNetworkLastKnownNodesResponse;
import io.coti.basenode.http.data.NetworkLastKnownNodesResponseData;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface INetworkService {

    void init();

    void handleNetworkChanges(NetworkData networkData);

    void verifyNodeManager(NetworkData newNetworkData);

    void verifyNodeManager(NetworkLastKnownNodesResponseData networkLastKnownNodesResponseData);

    String getRecoveryServerAddress();

    NetworkNodeData getRecoveryServer();

    void setRecoveryServer(NetworkNodeData recoveryServer);

    Map<Hash, NetworkNodeData> getMapFromFactory(NodeType nodeType);

    NetworkNodeData getSingleNodeData(NodeType nodeType);

    void addNode(NetworkNodeData networkNodeData);

    void removeNode(NetworkNodeData networkNodeData);

    void validateNetworkNodeData(NetworkNodeData networkNodeData);

    boolean validateFeeData(FeeData feeData);

    List<NetworkNodeData> getShuffledNetworkNodeDataListFromMapValues(NodeType nodeType);

    List<NetworkNodeData> getNetworkNodeDataList();

    void addListToSubscription(Collection<NetworkNodeData> nodeDataList);

    void handleConnectedDspNodesChange(List<NetworkNodeData> connectedDspNodes, Map<Hash, NetworkNodeData> newDspNodeMap, NodeType nodeType);

    void handleConnectedSingleNodeChange(NetworkData newNetworkData, NodeType singleNodeType);

    String getNodeManagerPropagationAddress();

    void setNodeManagerPropagationAddress(String nodeManagerPropagationAddress);

    void setConnectToNetworkUrl(String connectToNetworkUrl);

    NetworkData getNetworkData();

    void setNetworkData(NetworkData networkData);

    NetworkData getSignedNetworkData();

    NetworkNodeData getNetworkNodeData();

    void setNetworkNodeData(NetworkNodeData networkNodeData);

    void connectToNetwork();

    String getHost(String webServerUrl);

    boolean isZeroSpendServerInNetwork();

    GetNetworkLastKnownNodesResponse getSignedNetworkLastKnownNodesResponse();

    List<Hash> getNodesHashes(NodeType nodeType);

    Map<Hash, NetworkNodeData> getNetworkLastKnownNodeMap();

    void setNetworkLastKnownNodeMap(HashMap<Hash, NetworkNodeData> networkLastKnownNodeMap);

    boolean isConnectedToRecovery();

    void sendDataToConnectedDspNodes(IPropagatable propagatable);

    boolean isNotConnectedToDspNodes();

    Map<String, List<String>> getNetworkSummary();
}
