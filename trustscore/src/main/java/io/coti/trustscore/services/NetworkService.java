package io.coti.trustscore.services;

import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.services.CommunicationService;
import io.coti.basenode.services.interfaces.IIpService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Service
@Slf4j
public class NetworkService implements INetworkService {

    private NetworkDetails networkDetails;

    private String recoveryServerAddress;

    @Value("${server.port}")
    private String serverPort;


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
        NetworkNodeData zerospendNetworkNodeData = newNetworkDetails.getZerospendServer();
        if (zerospendNetworkNodeData != null && zerospendNetworkNodeData != this.networkDetails.getZerospendServer()) {
            log.info("Zero spend server {} is about to be added", zerospendNetworkNodeData.getHttpFullAddress());
            recoveryServerAddress = zerospendNetworkNodeData.getHttpFullAddress();
            communicationService.addSubscription(zerospendNetworkNodeData.getPropagationFullAddress());
        }
        List<NetworkNodeData> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(newNetworkDetails.getDspNetworkNodesList(),
                this.networkDetails.getDspNetworkNodesList()));
        if (!dspNodesToConnect.isEmpty()) {
            dspNodesToConnect.removeIf(dsp -> dsp.getAddress().equals(ipService.getIp()) && dsp.getHttpPort().equals(serverPort));
            Collections.shuffle(dspNodesToConnect);
            dspNodesToConnect.forEach(dspnode -> {
                        log.info("Dsp {} is about to be added", "http://" + dspnode.getAddress() + ":" + dspnode.getHttpPort());
                        communicationService.addSubscription(dspnode.getPropagationFullAddress());
                    }
            );
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


}
