package io.coti.nodemanager.services.interfaces;

import io.coti.basenode.data.NetworkNodeData;
import io.coti.nodemanager.http.data.SingleNodeDetailsForWallet;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface INodeManagementService {

    void init();

    void propagateNetworkChanges();

    ResponseEntity<String> addNode(NetworkNodeData networkNodeData);

    void insertDeletedNodeRecord(NetworkNodeData networkNodeData);

    Map<String, List<SingleNodeDetailsForWallet>> getNetworkDetailsForWallet();
}
