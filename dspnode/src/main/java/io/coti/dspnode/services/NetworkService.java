package io.coti.dspnode.services;

import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.services.BaseNodeNetworkService;
import io.coti.basenode.services.CommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class NetworkService extends BaseNodeNetworkService {

    @Value("${server.ip}")
    protected String nodeIp;
    @Value("${server.port}")
    private String serverPort;

    @Autowired
    private CommunicationService communicationService;

    @Override
    public void handleNetworkChanges(NetworkDetails newNetworkDetails) {
        log.info("New newNetworkDetails structure received: {}", networkDetailsService.getNetWorkSummary(newNetworkDetails));
        newNetworkDetails.getDspNetworkNodesMap().entrySet().removeIf(entry -> entry.getValue().getAddress().equals(nodeIp) && entry.getValue().getHttpPort().equals(serverPort));
        removeNodeFromSelfNetwork(newNetworkDetails);
        NetworkNodeData zerospendNetworkNodeData = newNetworkDetails.getZerospendServer();
        List<NetworkNodeData> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(newNetworkDetails.getDspNetworkNodesMap().values(),
                networkDetailsService.getNetworkDetails().getDspNetworkNodesMap().values()));
        if (dspNodesToConnect.size() > 0) {
            Collections.shuffle(dspNodesToConnect);
            addListToSubscriptionAndNetwork(dspNodesToConnect);
        }
        if (zerospendNetworkNodeData != null && recoveryServerAddress.isEmpty()) {
            log.info("Zero spend server {} is about to be added", zerospendNetworkNodeData.getHttpFullAddress());
            recoveryServerAddress = zerospendNetworkNodeData.getHttpFullAddress();
            communicationService.addSender(zerospendNetworkNodeData.getReceivingFullAddress());
            communicationService.addSubscription(zerospendNetworkNodeData.getPropagationFullAddress());
        }
        networkDetailsService.setNetworkDetails(newNetworkDetails);
    }


}
