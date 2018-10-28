package io.coti.trustscore.services;

import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NetworkNode;
import io.coti.basenode.services.CommunicationService;
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

    private NetworkData networkData;

    private String recoveryServerAddress;

    @Value("${server.port}")
    private String serverPort;
    @Value("${server.ip}")
    private String nodeIp;

    @Autowired
    private CommunicationService communicationService;

    @PostConstruct
    private void init(){
        networkData = new NetworkData();
    }

    @Override
    public void connectToCurrentNetwork() {
        handleNetworkChanges(networkData);
    }

    @Override
    public void handleNetworkChanges(NetworkData newNetworkData) {
        log.info("New newNetworkData structure received: {}", newNetworkData);
        NetworkNode zerospendNetworkNode = newNetworkData.getZerospendServer();
        if (zerospendNetworkNode != null && zerospendNetworkNode != this.networkData.getZerospendServer()) {
            log.info("Zero spend server {} is about to be added", zerospendNetworkNode.getHttpFullAddress());
            recoveryServerAddress = zerospendNetworkNode.getHttpFullAddress();
            communicationService.addSubscription(zerospendNetworkNode.getPropagationFullAddress());
        }
        List<NetworkNode> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(newNetworkData.getDspNetworkNodes(),
                this.networkData.getDspNetworkNodes()));
        dspNodesToConnect.removeIf(dsp -> dsp.getAddress().equals(nodeIp) && dsp.getHttpPort().equals(serverPort));
        if (dspNodesToConnect.size() > 0) {
            Collections.shuffle(dspNodesToConnect);
            dspNodesToConnect.forEach(dspnode -> {
                        log.info("Dsp {} is about to be added", "http://" + dspnode.getAddress() + ":" + dspnode.getHttpPort());
                communicationService.addSubscription(dspnode.getPropagationFullAddress());
                    }
            );
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


}
