package io.coti.zerospend.services;

import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.BaseNodeNetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NetworkService extends BaseNodeNetworkService {

    @Override
    public void handleNetworkChanges(NetworkData newNetworkData) {
        try {
            super.handleNetworkChanges(newNetworkData);

            handleConnectedNodesChange(NodeType.DspNode, newNetworkData, NodeType.ZeroSpendServer);
            handleConnectedNodesChange(NodeType.HistoryNode, newNetworkData, NodeType.ZeroSpendServer);
            handleConnectedSingleNodeChange(newNetworkData, NodeType.FinancialServer, NodeType.ZeroSpendServer);

            setNetworkData(newNetworkData);
        } catch (Exception e) {
            log.error("Handle network changes error", e);
        }
    }

    @Override
    public boolean isNodeConnectedToNetwork(NetworkData newNetworkData) {
        NetworkNodeData zeroSpendServerData = newNetworkData.getSingleNodeNetworkDataMap().get(NodeType.ZeroSpendServer);
        return zeroSpendServerData != null && zeroSpendServerData.equals(networkNodeData);
    }
}
