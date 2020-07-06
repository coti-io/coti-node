package io.coti.trustscore.services;

import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.BaseNodeNetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class NetworkService extends BaseNodeNetworkService {

    @Override
    public void handleNetworkChanges(NetworkData newNetworkData) {
        super.handleNetworkChanges(newNetworkData);

        handleConnectedNodesChange(NodeType.DspNode, newNetworkData, NodeType.TrustScoreNode);
        handleConnectedSingleNodeChange(newNetworkData, NodeType.ZeroSpendServer, NodeType.TrustScoreNode);
        handleConnectedSingleNodeChange(newNetworkData, NodeType.FinancialServer, NodeType.TrustScoreNode);

        setNetworkData(newNetworkData);

    }

    @Override
    public boolean isNodeConnectedToNetwork(NetworkData newNetworkData) {
        return newNetworkData.getMultipleNodeMaps().get(NodeType.TrustScoreNode).get(networkNodeData.getNodeHash()) != null;
    }


}
