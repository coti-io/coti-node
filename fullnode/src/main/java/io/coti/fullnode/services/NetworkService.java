package io.coti.fullnode.services;

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
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class NetworkService implements INetworkService {

    private NetworkDetails networkDetails;

    @Autowired
    private CommunicationService communicationService;

    private String recoveryServerAddress;

    @Autowired
    private IIpService ipService;

    @PostConstruct
    private void init() {
        networkDetails = new NetworkDetails();
    }

    @Override
    public void handleNetworkChanges(NetworkDetails newNetworkDetails) {
        log.info("New newNetworkDetails structure received: {}", newNetworkDetails.getNetWorkSummary());
        ipService.modifyNetworkDetailsIfNeeded(newNetworkDetails);
        this.networkDetails.getDspNetworkNodesList().forEach(dsp -> {
            if (!newNetworkDetails.getDspNetworkNodesList().contains(dsp)) {
                log.info("dsp {} is about disconnect from subscribing and receiving ", dsp.getHttpFullAddress());
                communicationService.removeSubscription(dsp.getPropagationFullAddress(), dsp.getNodeType());
                communicationService.removeSender(dsp.getReceivingFullAddress(), dsp.getNodeType());
            }
        });
        List<NetworkNodeData> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(newNetworkDetails.getDspNetworkNodesList(),
                this.networkDetails.getDspNetworkNodesList()));
        if (!dspNodesToConnect.isEmpty()){
            Collections.shuffle(dspNodesToConnect);
            recoveryServerAddress = dspNodesToConnect.get(0).getHttpFullAddress();
            if (networkDetails.getDspNetworkNodesList().size() == 1) {
                addDsp(dspNodesToConnect.get(0));
            } else if (networkDetails.getDspNetworkNodesList().isEmpty()) {
                addDsp(dspNodesToConnect.get(0));
                if (dspNodesToConnect.size() > 1) {
                    addDsp(dspNodesToConnect.get(1));
                }
            }
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
        return recoveryServerAddress;
    }

    @Override
    public void setRecoveryServerAddress(String recoveryServerAddress) {
        this.recoveryServerAddress = recoveryServerAddress;
    }

    private void addDsp(NetworkNodeData dspNetworkNodeData) {
        log.info("Dsp {} is about to be added", dspNetworkNodeData.getHttpFullAddress());
        networkDetails.addNode(dspNetworkNodeData);
        communicationService.addSubscription(dspNetworkNodeData.getPropagationFullAddress());
        communicationService.addSender(dspNetworkNodeData.getReceivingFullAddress());
    }

}
