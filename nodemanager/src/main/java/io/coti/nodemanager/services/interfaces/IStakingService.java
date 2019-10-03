package io.coti.nodemanager.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.http.interfaces.IResponse;
import io.coti.nodemanager.data.StakingNodeData;
import io.coti.nodemanager.http.SetNodeStakeRequest;
import io.coti.nodemanager.http.data.SingleNodeDetailsForWallet;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface IStakingService {

    ResponseEntity<IResponse> setNodeStake(StakingNodeData stakingNodeData);

    ResponseEntity<IResponse> setNodeStake(SetNodeStakeRequest setNodeStakeRequest);

    ResponseEntity<IResponse> getStakerList();

    NetworkNodeData selectStakedNode(Map<Hash, NetworkNodeData> nodeMap);

    boolean filterFullNode(SingleNodeDetailsForWallet singleNodeDetailsForWallet);
}
