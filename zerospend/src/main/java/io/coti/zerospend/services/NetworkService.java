package io.coti.zerospend.services;

import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.services.BaseNodeNetworkService;
import io.coti.basenode.services.CommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
public class NetworkService extends BaseNodeNetworkService {
    @Autowired
    private CommunicationService communicationService;

    @Override
    public void handleNetworkChanges(NetworkDetails newNetworkDetails) {
        log.info("New newNetworkDetails structure received: {}", networkDetailsService.getNetWorkSummary(newNetworkDetails));
        List<NetworkNodeData> nodesToRemove = new LinkedList<>();
        networkDetailsService.getNetworkDetails().getDspNetworkNodesMap().forEach((hash, node) -> {
            if (!newNetworkDetails.getDspNetworkNodesMap().containsKey(hash)) {
                log.info("node {} is about disconnect from subscribing and receiving ", node.getHttpFullAddress());
                communicationService.removeSubscription(node.getPropagationFullAddress(), node.getNodeType());
                nodesToRemove.add(node);
            }
        });
        nodesToRemove.forEach(dspNode -> networkDetailsService.removeNode(dspNode));
        List<NetworkNodeData> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(
                newNetworkDetails.getDspNetworkNodesMap().values(), networkDetailsService.getNetworkDetails().getDspNetworkNodesMap().values()
        ));
        addListToSubscriptionAndNetwork(dspNodesToConnect);
        networkDetailsService.setNetworkDetails(newNetworkDetails);
    }


}
