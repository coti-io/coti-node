package io.coti.financialserver.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.exceptions.NetworkChangeException;
import io.coti.basenode.services.BaseNodeNetworkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class NetworkService extends BaseNodeNetworkService {

    @Override
    public void handleNetworkChanges(NetworkData newNetworkData) {
        try {
            super.handleNetworkChanges(newNetworkData);
        } catch (NetworkChangeException e) {
            log.info(e.getMessage());
            return;
        }

        Map<Hash, NetworkNodeData> newDspNodeMap = newNetworkData.getMultipleNodeMaps().get(NodeType.DspNode);
        List<NetworkNodeData> connectedDspNodes = new ArrayList<>(getMapFromFactory(NodeType.DspNode).values());

        handleConnectedDspNodesChange(connectedDspNodes, newDspNodeMap, NodeType.FinancialServer);

        List<NetworkNodeData> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(newNetworkData.getMultipleNodeMaps().get(NodeType.DspNode).values(),
                getMapFromFactory(NodeType.DspNode).values()));
        addListToSubscription(dspNodesToConnect);

        handleConnectedSingleNodeChange(newNetworkData, NodeType.ZeroSpendServer, NodeType.FinancialServer);

        setNetworkData(newNetworkData);

    }

    @Override
    public boolean isNodeConnectedToNetwork(NetworkData newNetworkData) {
        NetworkNodeData financialServerData = newNetworkData.getSingleNodeNetworkDataMap().get(NodeType.FinancialServer);
        return financialServerData != null && financialServerData.equals(networkNodeData);
    }
}
