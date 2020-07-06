package io.coti.dspnode.services;

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

        handleConnectedNodesChange(NodeType.DspNode, newNetworkData, NodeType.DspNode);
        handleConnectedNodesChange(NodeType.HistoryNode, newNetworkData, NodeType.DspNode);
        handleConnectedSingleNodeChange(newNetworkData, NodeType.ZeroSpendServer, NodeType.DspNode);
        handleConnectedSingleNodeChange(newNetworkData, NodeType.FinancialServer, NodeType.DspNode);

        setNetworkData(newNetworkData);
    }

    @Override
    public boolean isNodeConnectedToNetwork(NetworkData newNetworkData) {
        return newNetworkData.getMultipleNodeMaps().get(NodeType.DspNode).get(networkNodeData.getNodeHash()) != null;
    }
}
