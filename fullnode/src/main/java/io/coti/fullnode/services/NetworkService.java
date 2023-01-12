package io.coti.fullnode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.exceptions.NetworkChangeException;
import io.coti.basenode.services.BaseNodeNetworkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.coti.basenode.services.BaseNodeServiceManager.zeroMQSender;
import static io.coti.fullnode.services.NodeServiceManager.communicationService;

@Service
@Slf4j
@Primary
public class NetworkService extends BaseNodeNetworkService {

    private final List<NetworkNodeData> connectedDspNodes = new ArrayList<>(2);

    @Override
    public void handleNetworkChanges(NetworkData newNetworkData) {
        try {
            super.handleNetworkChanges(newNetworkData);
        } catch (NetworkChangeException e) {
            log.info(e.getMessage());
            return;
        }

        Map<Hash, NetworkNodeData> newDspNodeMap = newNetworkData.getMultipleNodeMaps().get(NodeType.DspNode);

        handleConnectedDspNodesChange(connectedDspNodes, newDspNodeMap, NodeType.FullNode);

        if (connectedDspNodes.size() < 2) {
            List<NetworkNodeData> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(newDspNodeMap.values(), connectedDspNodes));
            Collections.shuffle(dspNodesToConnect);
            for (int i = 0; i < dspNodesToConnect.size() && i < 2 - connectedDspNodes.size(); i++) {
                if (i == 0 && recoveryServer == null) {
                    recoveryServer = dspNodesToConnect.get(0);
                }
                communicationService.addSubscription(dspNodesToConnect.get(i).getPropagationFullAddress(), NodeType.DspNode);
                communicationService.addSender(dspNodesToConnect.get(i).getReceivingFullAddress(), NodeType.DspNode);
                connectedDspNodes.add(dspNodesToConnect.get(i));
            }
        }

        setNetworkData(newNetworkData);
    }

    public void addToConnectedDspNodes(NetworkNodeData networkNodeData) {
        connectedDspNodes.add(networkNodeData);
    }

    @Override
    public void sendDataToConnectedDspNodes(IPropagatable propagatable) {
        connectedDspNodes.forEach(networkNodeData -> zeroMQSender.send(propagatable, networkNodeData.getReceivingFullAddress()));
    }

    @Override
    public boolean isNotConnectedToDspNodes() {
        return connectedDspNodes.isEmpty();
    }

    @Override
    public boolean isNodeConnectedToNetwork(NetworkData newNetworkData) {
        return newNetworkData.getMultipleNodeMaps().get(NodeType.FullNode).get(networkNodeData.getNodeHash()) != null;
    }

    @Override
    public boolean isConnectedToRecovery() {
        return !isNotConnectedToDspNodes();
    }

}
