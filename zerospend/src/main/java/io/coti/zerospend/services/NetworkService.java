package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.data.Network;
import io.coti.basenode.data.Node;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.services.CommunicationService;
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
    private Network network;

    @Autowired
    private CommunicationService communicationService;
    @Autowired
    private IPropagationSubscriber subscriber;

    @PostConstruct
    private void init(){
        network = new Network();
    }

    @Override
    public void handleNetworkChanges(Network newNetwork) {
        log.info("New newNetwork structure received: {}", newNetwork);
        List<Node> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(newNetwork.dspNodes, this.network.getDspNodes()));
        if(dspNodesToConnect.size() > 0){
            dspNodesToConnect.forEach(dspNode -> {
                log.info("Dsp {} is about to be added", dspNode.getHttpFullAddress());
                network.addNode(dspNode);
                subscriber.connectAndSubscribeToServer(dspNode.getPropagationFullAddress());
            });
        }
        this.network = newNetwork;
    }

    @Override
    public Network getNetwork() {
        return network;
    }

    @Override
    public void saveNetwork(Network network) {
        this.network = network;
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
        handleNetworkChanges(network);
    }


}
