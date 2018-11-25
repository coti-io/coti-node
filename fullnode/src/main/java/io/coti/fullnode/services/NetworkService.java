package io.coti.fullnode.services;

import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.services.BaseNodeNetworkService;
import io.coti.basenode.services.CommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Service
@Slf4j
public class NetworkService extends BaseNodeNetworkService {

    @Autowired
    private CommunicationService communicationService;

    @Override
    public void handleNetworkChanges(NetworkDetails newNetworkDetails) {
        log.info("New newNetworkDetails structure received: {}", networkDetailsService.getNetWorkSummary(newNetworkDetails));
        networkDetailsService.getNetworkDetails().getDspNetworkNodesMap().forEach((hash, dspNode) -> {
            if (!newNetworkDetails.getDspNetworkNodesMap().containsKey(hash)) {
                log.info("dsp {} is about disconnect from subscribing and receiving ", dspNode.getHttpFullAddress());
                communicationService.removeSubscription(dspNode.getPropagationFullAddress(), dspNode.getNodeType());
                communicationService.removeSender(dspNode.getReceivingFullAddress(), dspNode.getNodeType());
            }
        });
        List<NetworkNodeData> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(newNetworkDetails.getDspNetworkNodesMap().values(),
                networkDetailsService.getNetworkDetails().getDspNetworkNodesMap().values()));

        List<NetworkNodeData> twoDspNodes = new LinkedList<>();
        Collections.shuffle(dspNodesToConnect);
        for (int i = 0; i < dspNodesToConnect.size() && i < 2; i++) {
            if (i == 0 && recoveryServerAddress == null) {
                recoveryServerAddress = dspNodesToConnect.get(0).getHttpFullAddress();
            }
            twoDspNodes.add(dspNodesToConnect.get(i));
        }
        addListToSubscriptionAndNetwork(twoDspNodes);
        twoDspNodes.forEach(node -> communicationService.addSender(node.getReceivingFullAddress()));
        networkDetailsService.setNetworkDetails(newNetworkDetails);

    }

}
