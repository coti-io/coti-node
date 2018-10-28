package io.coti.zerospend.services;

import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NetworkNode;
import io.coti.basenode.services.CommunicationService;
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
    private NetworkData networkData;

    @Autowired
    private CommunicationService communicationService;


    @PostConstruct
    private void init() {
        networkData = new NetworkData();
    }

    @Override
    public void handleNetworkChanges(NetworkData newNetworkData) {
        log.info("New newNetworkData structure received: {}", newNetworkData);
        List<NetworkNode> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(newNetworkData.getDspNetworkNodes(), this.networkData.getDspNetworkNodes()));
        if (dspNodesToConnect.size() > 0) {
            dspNodesToConnect.forEach(dspNode -> {
                log.info("Dsp {} is about to be added", dspNode.getHttpFullAddress());
                networkData.addNode(dspNode);
                communicationService.addSubscription(dspNode.getPropagationFullAddress());
            });
        }
        this.networkData = newNetworkData;
    }

    @Override
    public NetworkData getNetworkData() {
        return networkData;
    }

    @Override
    public void saveNetwork(NetworkData networkData) {
        this.networkData = networkData;
    }

    @Override
    public String getRecoveryServerAddress() {
        return "";
    }

    @Override
    public void setRecoveryServerAddress(String recoveryServerAddress) {
    }

    @Override
    public void connectToCurrentNetwork() {
        handleNetworkChanges(networkData);
    }


}
