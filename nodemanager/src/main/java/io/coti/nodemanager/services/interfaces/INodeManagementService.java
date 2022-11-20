package io.coti.nodemanager.services.interfaces;

import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.nodemanager.data.NetworkNodeStatus;
import io.coti.nodemanager.http.AddNodePairEventRequest;
import io.coti.nodemanager.http.AddNodeSingleEventRequest;
import io.coti.nodemanager.http.UpdateNodeReservedHostRequest;
import io.coti.nodemanager.http.data.SingleNodeDetailsForWallet;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface INodeManagementService {

    void init();

    void propagateNetworkChanges();

    ResponseEntity<String> addNode(NetworkNodeData networkNodeData);

    Map<String, List<SingleNodeDetailsForWallet>> getNetworkDetailsForWallet(String healthState);

    SingleNodeDetailsForWallet getOneNodeDetailsForWallet();

    void addNodeHistory(NetworkNodeData networkNodeData, NetworkNodeStatus nodeStatus, Instant currentEventDateTime);

    ResponseEntity<IResponse> addSingleNodeEvent(AddNodeSingleEventRequest request);

    ResponseEntity<IResponse> addPairNodeEvent(AddNodePairEventRequest request);

    ResponseEntity<IResponse> getBlacklistedNodes();

    ResponseEntity<IResponse> updateNodeReservedHost(UpdateNodeReservedHostRequest request);
}
