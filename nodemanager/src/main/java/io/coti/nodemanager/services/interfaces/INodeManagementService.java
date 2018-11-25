package io.coti.nodemanager.services.interfaces;

import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.nodemanager.data.SingleNodeDetailsForWallet;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface INodeManagementService {

    void updateNetworkChanges();

    NetworkDetails newNode(NetworkNodeData networkNodeData) throws IllegalAccessException;

    void insertDeletedNodeRecord(NetworkNodeData networkNodeData);

    Map<String, List<SingleNodeDetailsForWallet>> createNetworkDetailsForWallet();

    LocalDateTime getUTCnow();
}
