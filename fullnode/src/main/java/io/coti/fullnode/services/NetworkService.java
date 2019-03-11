package io.coti.fullnode.services;

import io.coti.basenode.communication.interfaces.ISender;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.services.BaseNodeNetworkService;
import io.coti.basenode.services.interfaces.ICommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class NetworkService extends BaseNodeNetworkService {

    @Autowired
    private ICommunicationService communicationService;
    @Autowired
    private ISender sender;
    private List<NetworkNodeData> connectedDspNodes = new ArrayList<>(2);

    @Override
    public void handleNetworkChanges(NetworkData newNetworkData) {
        log.info("New network structure received");
        Map<Hash, NetworkNodeData> newDspNodeMap = newNetworkData.getMultipleNodeMaps().get(NodeType.DspNode);
        connectedDspNodes.removeIf(dspNode -> {
            boolean remove = !(newDspNodeMap.containsKey(dspNode.getNodeHash()) && newDspNodeMap.get(dspNode.getNodeHash()).getAddress().equals(dspNode.getAddress()));
            if (remove) {
                log.info("Disconnecting from dsp {} from subscribing and receiving", dspNode.getAddress());
                communicationService.removeSubscription(dspNode.getPropagationFullAddress(), NodeType.DspNode);
                communicationService.removeSender(dspNode.getReceivingFullAddress(), NodeType.DspNode);
                if (recoveryServerAddress.equals(dspNode.getHttpFullAddress())) {
                    recoveryServerAddress = null;
                }
            } else {
                if (!newDspNodeMap.get(dspNode.getNodeHash()).getPropagationPort().equals(dspNode.getPropagationPort())) {
                    communicationService.removeSubscription(dspNode.getPropagationFullAddress(), NodeType.DspNode);
                    communicationService.addSubscription(newDspNodeMap.get(dspNode.getNodeHash()).getPropagationFullAddress(), NodeType.DspNode);
                    dspNode.setPropagationPort(newDspNodeMap.get(dspNode.getNodeHash()).getPropagationPort());
                }
                if (!newDspNodeMap.get(dspNode.getNodeHash()).getReceivingPort().equals(dspNode.getReceivingPort())) {
                    communicationService.removeSender(dspNode.getReceivingFullAddress(), NodeType.DspNode);
                    communicationService.addSender(newDspNodeMap.get(dspNode.getNodeHash()).getReceivingFullAddress());
                    dspNode.setReceivingPort(newDspNodeMap.get(dspNode.getNodeHash()).getReceivingPort());
                }
            }
            return remove;
        });
        if (connectedDspNodes.size() < 2) {
            List<NetworkNodeData> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(newNetworkData.getMultipleNodeMaps().get(NodeType.DspNode).values(), connectedDspNodes));
            Collections.shuffle(dspNodesToConnect);
            for (int i = 0; i < dspNodesToConnect.size() && i < 2 - connectedDspNodes.size(); i++) {
                if (i == 0 && recoveryServerAddress == null) {
                    recoveryServerAddress = dspNodesToConnect.get(0).getHttpFullAddress();
                }
                communicationService.addSubscription(dspNodesToConnect.get(i).getPropagationFullAddress(), NodeType.DspNode);
                communicationService.addSender(dspNodesToConnect.get(i).getReceivingFullAddress());
            }
        }

        setNetworkData(newNetworkData);

    }

    public void addToConnectedDspNodes(NetworkNodeData networkNodeData) {
        connectedDspNodes.add(networkNodeData);
    }

    public void sendDataToConnectedDspNodes(IPropagatable propagatable) {
        connectedDspNodes.forEach(networkNodeData -> sender.send(propagatable, networkNodeData.getReceivingFullAddress()));
    }

}
