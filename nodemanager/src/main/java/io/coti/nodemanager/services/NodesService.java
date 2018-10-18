package io.coti.nodemanager.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.Network;
import io.coti.basenode.data.Node;
import io.coti.basenode.data.NodeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class NodesService {
    private Network network;
    @Autowired
    private IPropagationPublisher propagationPublisher;

    private String PROPAGATION_PORT = "1234";

    @PostConstruct
    public void init() {
        propagationPublisher.init(PROPAGATION_PORT);
    }

    public void updateNetworkChanges() {
        log.info("Propagating network changes...");
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
           log.error("An error was thrown",e);
        }
        propagationPublisher.propagate(network, Arrays.asList(NodeType.FullNode, NodeType.ZeroSpendServer, NodeType.DspNode));
    }

    public NodesService() {
        network = new Network();
        network.nodeManagerPropagationAddress = "tcp://localhost:" + PROPAGATION_PORT;
    }

    public Network newNode(Node node) {
        log.info("New node received: {}", node);
        if(!validateNodeProperties(node)){
            log.info("Illegal node properties received: {}", node);
        }
        this.network.addNode(node);
        updateNetworkChanges();
        return network;
    }

    private boolean validateNodeProperties(Node node) {
        return true;
    }

    public Network getAllNodes() {
        return network;
    }


}