package io.coti.nodemanager.services.interfaces;

import io.coti.basenode.data.FeeData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.nodemanager.http.SetNodeStakeAdminRequest;
import io.coti.nodemanager.http.SetNodeStakeRequest;
import io.coti.nodemanager.http.data.SingleNodeDetailsForWallet;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface IStakingService {

    ResponseEntity<IResponse> setNodeStake(SetNodeStakeAdminRequest setNodeStakeAdminRequest);

    ResponseEntity<IResponse> setNodeStake(SetNodeStakeRequest setNodeStakeRequest);

    ResponseEntity<IResponse> getStakersList();

    ResponseEntity<String> distributionCheck(FeeData request);

    Hash selectNode(Map<Hash, NetworkNodeData> nodeMap);

    boolean filterFullNodes(SingleNodeDetailsForWallet singleNodeDetailsForWallet);
}
