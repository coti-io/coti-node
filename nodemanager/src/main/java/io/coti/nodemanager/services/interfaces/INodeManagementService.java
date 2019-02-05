package io.coti.nodemanager.services.interfaces;

import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.nodemanager.http.data.SingleNodeDetailsForWallet;

import java.util.List;
import java.util.Map;

public interface INodeManagementService {

    void propagateNetworkChanges();

    NetworkData newNode(NetworkNodeData networkNodeData) throws IllegalAccessException;

    void insertDeletedNodeRecord(NetworkNodeData networkNodeData);

    Map<String, List<SingleNodeDetailsForWallet>> getNetworkDetailsForWallet();
}
