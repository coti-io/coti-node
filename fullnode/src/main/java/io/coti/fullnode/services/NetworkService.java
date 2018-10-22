package io.coti.fullnode.services;

import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.data.Network;
import io.coti.basenode.data.Node;
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

    private Network network;

    @Autowired
    private CommunicationService communicationService;

    private String recoveryServerAddress;
    @Autowired
    private IPropagationSubscriber subscriber;


    @PostConstruct
    private void init(){
        network = new Network();
    }

    @Override
    public void connectToCurrentNetwork(){
        handleNetworkChanges(network);
    }

    @Override
    public void handleNetworkChanges(Network newNetwork) {
        log.info("New newNetwork structure received: {}", newNetwork);
        List<Node> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(newNetwork.dspNodes, this.network.getDspNodes()));
        Collections.shuffle(dspNodesToConnect);
        if (dspNodesToConnect.size() > 0) {
            recoveryServerAddress = dspNodesToConnect.get(0).getHttpFullAddress();
            if (this.network.getDspNodes().size() == 1){
                addDsp(dspNodesToConnect.get(0));
            }
            else if(this.network.getDspNodes().isEmpty()) {
                addDsp(dspNodesToConnect.get(0));
                if(dspNodesToConnect.size() > 1) {
                    addDsp(dspNodesToConnect.get(1));
                }
            }
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
        return recoveryServerAddress;
    }

    @Override
    public void setRecoveryServerAddress(String recoveryServerAddress) {
        this.recoveryServerAddress = recoveryServerAddress;
    }

    private void addDsp(Node dspNode){
            log.info("Dsp {} is about to be added",dspNode.getHttpFullAddress());
            network.addNode(dspNode);
            subscriber.connectAndSubscribeToServer(dspNode.getPropagationFullAddress());//communicationService.addSubscription(dspNode.getAddress(), dspNode.getPropagationPort());
            //subscriber.subscribeAll(dspNode.getPropagationFullAddress());

            communicationService.addSender(dspNode.getAddress(), dspNode.getPropagationPort());
    }

}
