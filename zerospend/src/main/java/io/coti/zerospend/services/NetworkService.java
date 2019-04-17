package io.coti.zerospend.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
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

            Map<Hash, NetworkNodeData> newDspNodeMap = newNetworkData.getMultipleNodeMaps().get(NodeType.DspNode);
            List<NetworkNodeData> connectedDspNodes = new ArrayList<>(getMapFromFactory(NodeType.DspNode).values());

            handleConnectedDspNodesChange(connectedDspNodes, newDspNodeMap, NodeType.ZeroSpendServer);

            List<NetworkNodeData> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(
                    newNetworkData.getMultipleNodeMaps().get(NodeType.DspNode).values(), connectedDspNodes
            ));
            addListToSubscription(dspNodesToConnect);

            handleConnectedSingleNodeChange(newNetworkData, NodeType.FinancialServer, NodeType.ZeroSpendServer);

            setNetworkData(newNetworkData);
        } catch (Exception e) {
            log.error("Handle network changes error");
            e.printStackTrace();
        }
    }

    @Override
    public boolean isNodeConnectedToNetwork(NetworkData newNetworkData) {
        NetworkNodeData zeroSpendServerData = newNetworkData.getSingleNodeNetworkDataMap().get(NodeType.ZeroSpendServer);
        return zeroSpendServerData != null && zeroSpendServerData.equals(networkNodeData);
    }
}
