package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.data.Network;
import io.coti.basenode.data.Node;
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

    private Network network;

    @Value("${server.port}")
    private String serverPort;
    @Value("${server.ip}")
    private String nodeIp;


    private String recoveryServerAddress;
    @Autowired
    private CommunicationService communicationService;
    @Autowired
    private IPropagationSubscriber subscriber;

    @PostConstruct
    private void init(){
        network = new Network();
    }

    @Override
    public void connectToCurrentNetwork() {
        handleNetworkChanges(network);
    }

    @Override
    public void handleNetworkChanges(Network newNetwork) {
        log.info("New newNetwork structure received: {}", newNetwork);
            Node zerospendNode = newNetwork.getZerospendServer();
        if (zerospendNode != null && recoveryServerAddress.isEmpty() ) {
            log.info("Zero spend server {} is about to be added", zerospendNode.getHttpFullAddress());
            recoveryServerAddress = zerospendNode.getHttpFullAddress();
            communicationService.addSender(zerospendNode.getReceivingFullAddress());
            subscriber.connectAndSubscribeToServer(zerospendNode.getPropagationFullAddress());
        }
        List<Node> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(newNetwork.dspNodes, this.network.getDspNodes()));
        dspNodesToConnect.removeIf(dsp -> dsp.getAddress().equals(nodeIp) && dsp.getHttpPort().equals(serverPort));
        if (dspNodesToConnect.size() > 0) {
            Collections.shuffle(dspNodesToConnect);
            dspNodesToConnect.forEach(dspnode -> {
                log.info("Dsp {} is about to be added to dsp list", dspnode.getHttpFullAddress());
                subscriber.connectAndSubscribeToServer(dspnode.getPropagationFullAddress());
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

    public String getRecoveryServerAddress() {
        return recoveryServerAddress;
    }

    public void setRecoveryServerAddress(String recoveryServerAddress) {
        this.recoveryServerAddress = recoveryServerAddress;
    }

}
