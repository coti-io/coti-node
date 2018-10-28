package io.coti.fullnode.services;

import io.coti.basenode.data.NetworkNode;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;
import io.coti.basenode.services.interfaces.INetworkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

@Service
public class InitializationService extends BaseNodeInitializationService {
    @Autowired
    private CommunicationService communicationService;
    @Value("${server.port}")
    private String serverPort;
    @Value("${server.ip}")
    private String nodeIp;
    @Autowired
    private INetworkService networkService;

    @PostConstruct
    public void init() {
        super.connectToNetwork();
        communicationService.initSubscriber(NodeType.FullNode);
        List<NetworkNode> dspNetworkNodes = this.networkService.getNetworkData().getDspNetworkNodes();
        Collections.shuffle(dspNetworkNodes);
        NetworkNode firstDspNetworkNode = null;
        if (dspNetworkNodes.size() > 0) {
            firstDspNetworkNode = dspNetworkNodes.get(0);
            networkService.setRecoveryServerAddress(firstDspNetworkNode.getHttpFullAddress());
        }

        if (firstDspNetworkNode != null) {
            communicationService.addSender(firstDspNetworkNode.getReceivingFullAddress());
            communicationService.addSubscription(firstDspNetworkNode.getPropagationFullAddress());
            networkService.getNetworkData().addNode(firstDspNetworkNode);
        }
        if (dspNetworkNodes.size() > 1) { //TODO: subscribing only to one dsp ?
            communicationService.addSender(dspNetworkNodes.get(1).getReceivingFullAddress());
            communicationService.addSubscription(dspNetworkNodes.get(1).getPropagationFullAddress());
            networkService.getNetworkData().addNode(dspNetworkNodes.get(1));

        }
        super.init();
    }

    protected NetworkNode getNodeProperties() {
        return new NetworkNode(NodeType.FullNode, nodeIp, serverPort);
    }
}