package io.coti.zerospend.services;

import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.services.CommunicationService;
import io.coti.basenode.services.interfaces.IIpService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class NetworkService implements INetworkService {
    private NetworkDetails networkDetails;

    @Autowired
    private CommunicationService communicationService;
    @Autowired
    private IIpService ipService;

    @PostConstruct
    private void init() {
        networkDetails = new NetworkDetails();
    }

    @Override
    public void handleNetworkChanges(NetworkDetails newNetworkDetails) {
        log.info("New newNetworkDetails structure received: {}", newNetworkDetails);
        ipService.modifyNetworkDetailsIfNeeded(newNetworkDetails);
        List<NetworkNodeData> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(newNetworkDetails.getDspNetworkNodesList(), this.networkDetails.getDspNetworkNodesList()));
        if (!dspNodesToConnect.isEmpty()) {
            dspNodesToConnect.forEach(dspNode -> {
                log.info("Dsp {} is about to be added", dspNode.getHttpFullAddress());
                networkDetails.addNode(dspNode);
                communicationService.addSubscription(dspNode.getPropagationFullAddress());
            });
        }
        this.networkDetails = newNetworkDetails;
    }

    @Override
    public NetworkDetails getNetworkDetails() {
        return networkDetails;
    }

    @Override
    public void saveNetwork(NetworkDetails networkDetails) {
        ipService.modifyNetworkDetailsIfNeeded(networkDetails);
        this.networkDetails = networkDetails;
    }

    @Override
    public String getRecoveryServerAddress() {
        return "";
    }

    @Override
    public void setRecoveryServerAddress(String recoveryServerAddress) {
    }

}
