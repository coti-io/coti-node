package io.coti.fullnode.services;

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

    @PostConstruct
    private void init() {
        network = new Network();
    }

    @Override
    public void connectToCurrentNetwork() {
        handleNetworkChanges(network);
    }

    @Override
    public void handleNetworkChanges(Network newNetwork) {
        log.info("New newNetwork structure received: {}", newNetwork);
        newNetwork.dspNodes.removeIf(dsp -> {
            if (!this.network.getDspNodes().contains(dsp)) {
                log.info("dsp {} is about disconnect from subscribing and receiving ", dsp.getHttpFullAddress());
                communicationService.removeSubscription(dsp.getPropagationFullAddress(), dsp.getNodeType());
                communicationService.removeSender(dsp.getReceivingFullAddress(), dsp.getNodeType());
                return true;
            }
            return false;
        });
        List<Node> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(newNetwork.getDspNodes(), this.network.getDspNodes()));
        Collections.shuffle(dspNodesToConnect);
        if (!dspNodesToConnect.isEmpty()){//(this.network.getDspNodes().size() > 0) {
            recoveryServerAddress = dspNodesToConnect.get(0).getHttpFullAddress();
            if (network.getDspNodes().size() == 1) {
                addDsp(dspNodesToConnect.get(0));
            } else if (network.getDspNodes().isEmpty()) {
                addDsp(dspNodesToConnect.get(0));
                if (dspNodesToConnect.size() > 1) {
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

    private void addDsp(Node dspNode) {
        log.info("Dsp {} is about to be added", dspNode.getHttpFullAddress());
        network.addNode(dspNode);
        communicationService.addSubscription(dspNode.getPropagationFullAddress());
        communicationService.addSender(dspNode.getReceivingFullAddress());
    }

}
