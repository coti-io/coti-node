package io.coti.nodemanager.services.interfaces;

import io.coti.basenode.data.NetworkNodeData;
import io.coti.nodemanager.data.NetworkNodeStatus;
import io.coti.nodemanager.http.data.SingleNodeDetailsForWallet;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface INodeManagementService {

    void init();

    void propagateNetworkChanges();

    ResponseEntity<String> addNode(NetworkNodeData networkNodeData);

    Map<String, List<SingleNodeDetailsForWallet>> getNetworkDetailsForWallet();

    SingleNodeDetailsForWallet getOneNodeDetailsForWallet();

    void writeNodeHistory(NetworkNodeData networkNodeData, NetworkNodeStatus inactive, LocalDateTime eventDateTime);
}
