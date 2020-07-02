package io.coti.financialserver.services;

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
        super.handleNetworkChanges(newNetworkData);

        handleConnectedNodesChange(NodeType.DspNode, newNetworkData, NodeType.FinancialServer);
        handleConnectedSingleNodeChange(newNetworkData, NodeType.ZeroSpendServer, NodeType.FinancialServer);

        setNetworkData(newNetworkData);

    }

    @Override
    public boolean isNodeConnectedToNetwork(NetworkData newNetworkData) {
        NetworkNodeData financialServerData = newNetworkData.getSingleNodeNetworkDataMap().get(NodeType.FinancialServer);
        return financialServerData != null && financialServerData.equals(networkNodeData);
    }
}
