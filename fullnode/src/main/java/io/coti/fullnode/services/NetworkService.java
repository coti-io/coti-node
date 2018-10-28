package io.coti.fullnode.services;

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
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class NetworkService implements INetworkService {

    private NetworkData networkData;

    @Autowired
    private CommunicationService communicationService;

    private String recoveryServerAddress;

    @PostConstruct
    private void init() {
        networkData = new NetworkData();
    }

    @Override
    public void connectToCurrentNetwork() {
        handleNetworkChanges(networkData);
    }

    @Override
    public void handleNetworkChanges(NetworkData newNetworkData) {
        log.info("New newNetworkData structure received: {}", newNetworkData);
        this.networkData.getDspNetworkNodes().forEach(dsp -> {
            if (!newNetworkData.getDspNetworkNodes().contains(dsp)) {
                log.info("dsp {} is about disconnect from subscribing and receiving ", dsp.getHttpFullAddress());
                communicationService.removeSubscription(dsp.getPropagationFullAddress(), dsp.getNodeType());
                communicationService.removeSender(dsp.getReceivingFullAddress(), dsp.getNodeType());
            }
        });


        List<NetworkNode> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(newNetworkData.getDspNetworkNodes(), this.networkData.getDspNetworkNodes()));
        Collections.shuffle(dspNodesToConnect);
        if (!dspNodesToConnect.isEmpty()){//(this.networkData.getDspNetworkNodes().size() > 0) {
            recoveryServerAddress = dspNodesToConnect.get(0).getHttpFullAddress();
            if (networkData.getDspNetworkNodes().size() == 1) {
                addDsp(dspNodesToConnect.get(0));
            } else if (networkData.getDspNetworkNodes().isEmpty()) {
                addDsp(dspNodesToConnect.get(0));
                if (dspNodesToConnect.size() > 1) {
                    addDsp(dspNodesToConnect.get(1));
                }
            }
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
        return recoveryServerAddress;
    }

    @Override
    public void setRecoveryServerAddress(String recoveryServerAddress) {
        this.recoveryServerAddress = recoveryServerAddress;
    }

    private void addDsp(NetworkNode dspNetworkNode) {
        log.info("Dsp {} is about to be added", dspNetworkNode.getHttpFullAddress());
        networkData.addNode(dspNetworkNode);
        communicationService.addSubscription(dspNetworkNode.getPropagationFullAddress());
        communicationService.addSender(dspNetworkNode.getReceivingFullAddress());
    }

}
