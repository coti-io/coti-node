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

@Slf4j
@Service
public class NodesService {
    private Network network;
    @Autowired
    private IPropagationPublisher propagationPublisher;

    @PostConstruct
    public void init() {
        propagationPublisher.init("11111");
    }

    public void updateNetworkChanges() {
        log.info("Propagating network changes...");
        propagationPublisher.propagate(network, Arrays.asList(NodeType.FullNode, NodeType.ZeroSpendServer, NodeType.DspNode));
    }

    public NodesService() {
        network = new Network();
    }

    public void newNode(Node node) {
        log.info("New node received: {}", node);
        this.network.addNode(node);
        updateNetworkChanges();
    }

    public Network getAllNodes() {
        return network;
    }
}