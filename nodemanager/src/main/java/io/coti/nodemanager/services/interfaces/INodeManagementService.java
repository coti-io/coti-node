package io.coti.nodemanager.services.interfaces;

import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.nodemanager.data.NetworkNodeStatus;
import io.coti.nodemanager.http.AddNodeBeginEventPairAdminRequest;
import io.coti.nodemanager.http.AddNodeEventAdminRequest;
import io.coti.nodemanager.http.data.SingleNodeDetailsForWallet;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface INodeManagementService {

    void init();

    void propagateNetworkChanges();

    ResponseEntity<String> addNode(NetworkNodeData networkNodeData);

    Map<String, List<SingleNodeDetailsForWallet>> getNetworkDetailsForWallet();

    SingleNodeDetailsForWallet getOneNodeDetailsForWallet();

    void addNodeHistory(NetworkNodeData networkNodeData, NetworkNodeStatus nodeStatus, Instant currentEventDateTime);

    ResponseEntity<IResponse> addNodeBeginEventPairAdmin(AddNodeBeginEventPairAdminRequest request);

    ResponseEntity<IResponse> addSingleNodeEvent(AddNodeEventAdminRequest request);
}
