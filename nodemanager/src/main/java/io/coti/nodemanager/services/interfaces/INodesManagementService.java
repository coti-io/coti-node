package io.coti.nodemanager.services.interfaces;

import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.nodemanager.data.SingleNodeDetailsForWallet;

import java.util.List;
import java.util.Map;

public interface INodesManagementService {

    void updateNetworkChanges();

    NetworkDetails newNode(NetworkNodeData networkNodeData) throws IllegalAccessException;

    NetworkDetails getAllNetworkData();

    void insertDeletedNodeRecord(NetworkNodeData networkNodeData);

    Map<String, List<SingleNodeDetailsForWallet>> createNetworkDetailsForWallet();
}
