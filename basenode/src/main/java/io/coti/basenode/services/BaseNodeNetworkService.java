package io.coti.basenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.services.interfaces.INetworkDetailsService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class BaseNodeNetworkService implements INetworkService {

    protected String recoveryServerAddress;

    @Autowired
    protected INetworkDetailsService networkDetailsService;
    @Autowired
    private CommunicationService communicationService;

    public void handleNetworkChanges(NetworkDetails networkDetails) {
    }

    public String getRecoveryServerAddress() {
        return recoveryServerAddress;
    }

    public void setRecoveryServerAddress(String recoveryServerAddress) {
        this.recoveryServerAddress = recoveryServerAddress;
    }

    public void addListToSubscriptionAndNetwork(Collection< NetworkNodeData> nodeDataList) {
        Iterator<NetworkNodeData> nodeDataIterator = nodeDataList.iterator();
        while (nodeDataIterator.hasNext()) {
            NetworkNodeData node = nodeDataIterator.next();
            addAndSubscribeSingleNode(node);
        }
    }

    protected void addAndSubscribeSingleNode(NetworkNodeData node) {
        log.info("{} {} is about to be added to subscription and network", node.getNodeType(), node.getHttpFullAddress());
        networkDetailsService.addNode(node);
        communicationService.addSubscription(node.getPropagationFullAddress());
    }

    protected void removeNodeFromSelfNetwork(NetworkDetails newNetworkDetails) {
        List<NetworkNodeData> nodesToRemove = new LinkedList<>();
        networkDetailsService.getNetworkDetails().getDspNetworkNodesMap().forEach((hash, node) -> {
            if (!newNetworkDetails.getDspNetworkNodesMap().containsKey(hash)) {
                log.info("dsp {} is about disconnect from subscribing and receiving ", node.getHttpFullAddress());
                communicationService.removeSubscription(node.getPropagationFullAddress(), node.getNodeType());
                communicationService.removeSender(node.getReceivingFullAddress(), node.getNodeType());
                nodesToRemove.add(node);
            }
        });
        nodesToRemove.forEach(dspNode -> networkDetailsService.removeNode(dspNode));
    }


}
