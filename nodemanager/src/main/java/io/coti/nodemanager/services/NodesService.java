package io.coti.nodemanager.services;

import io.coti.basenode.communication.interfaces.IPropagationPublisher;
import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NetworkNode;
import io.coti.basenode.data.NodeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class NodesService {

    private NetworkData networkData;

    @Autowired
    private IPropagationPublisher propagationPublisher;

    @Value("${propagation.port}")
    private String propagationPort;


    @PostConstruct
    public void init() {
        networkData = new NetworkData();
        networkData.setNodeManagerPropagationAddress("tcp://localhost:" + propagationPort);
        propagationPublisher.init(propagationPort);
    }

    public void updateNetworkChanges() {
        log.info("Propagating networkData changes...");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            log.error("An error was thrown", e);
        }
        propagationPublisher.propagate(networkData, Arrays.asList(NodeType.FullNode, NodeType.ZeroSpendServer,
                NodeType.DspNode, NodeType.TrustScoreNode));
    }

    public NetworkData newNode(NetworkNode networkNode) {
        log.info("New networkNode received: {}", networkNode);
        if (!validateNodeProperties(networkNode)) {
            log.info("Illegal networkNode properties received: {}", networkNode);
        }
        this.networkData.addNode(networkNode);
        updateNetworkChanges();
        return networkData;
    }

    private boolean validateNodeProperties(NetworkNode networkNode) {
        return true;
    }

    public NetworkData getAllNetworkData() {
        return networkData;
    }


}