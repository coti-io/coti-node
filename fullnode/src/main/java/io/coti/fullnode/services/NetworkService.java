package io.coti.fullnode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.services.BaseNodeNetworkService;
import io.coti.basenode.services.CommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class NetworkService extends BaseNodeNetworkService {

    @Autowired
    private CommunicationService communicationService;

    @Override
    public void handleNetworkChanges(NetworkDetails newNetworkDetails) {
        log.info("New newNetworkDetails structure received: {}", networkDetailsService.getNetWorkSummary(newNetworkDetails));
        removeNodeFromSelfNetwork(newNetworkDetails);
        List<NetworkNodeData> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(newNetworkDetails.getDspNetworkNodesMap().values(),
                networkDetailsService.getNetworkDetails().getDspNetworkNodesMap().values()));
        Collections.shuffle(dspNodesToConnect);
        if (recoveryServerAddress == null) {
            recoveryServerAddress = dspNodesToConnect.get(0).getHttpFullAddress();
        }
        for (int i = 0; networkDetailsService.getNetworkDetails().getDspNetworkNodesMap().size() < 2 && i < dspNodesToConnect.size() && i < 2; i++) {
            addAndSubscribeSingleNode(dspNodesToConnect.get(i));
            communicationService.addSender(dspNodesToConnect.get(i).getReceivingFullAddress());
        }

        Map<Hash, NetworkNodeData> activeDsps = new ConcurrentHashMap<>(networkDetailsService.getNetworkDetails().getDspNetworkNodesMap());
        networkDetailsService.setNetworkDetails(newNetworkDetails);
        networkDetailsService.getNetworkDetails().setDspNetworkNodesMap(activeDsps);
        if (networkDetailsService.getNetworkDetails().getDspNetworkNodesMap().size() > 2) {
            log.error("Something went wrong, two many dsps are connected ", networkDetailsService.getNetworkDetails());
            System.exit(-1);
        }
    }

}
