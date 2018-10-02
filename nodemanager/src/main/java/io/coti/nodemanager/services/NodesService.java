package io.coti.nodemanager.services;

import io.coti.basenode.data.Network;
import io.coti.basenode.data.Node;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NodesService {
    private Network network;
    private String zeroSpendServerAddress;

    public NodesService() {
        network = new Network();
    }

    public void newNode(Node node) {
        log.info("New node received: {}", node);
        this.network.addNode(node);
    }

    public void newZeroSpendServer(String zeroSpendServerAddress) {
        log.info("Zero Spend address received: {}", zeroSpendServerAddress);
        this.zeroSpendServerAddress = zeroSpendServerAddress;
    }

    public Network getAllNodes() {
        return network;
    }

    public String getZeroSpendAddress() {
        return zeroSpendServerAddress;
    }
}