package io.coti.fullnode.services;

import io.coti.basenode.data.Node;
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
        List<Node> dspNodes = this.networkService.getNetwork().dspNodes;
        Collections.shuffle(dspNodes);
        Node firstDspNode = null;
        if (dspNodes.size() > 0) {
            firstDspNode = dspNodes.get(0);
            networkService.setRecoveryServerAddress(firstDspNode.getHttpFullAddress());
        }
        super.init();

        if (firstDspNode != null) {
            communicationService.addSender(firstDspNode.getAddress(), firstDspNode.getReceivingPort());
            communicationService.addSubscription(firstDspNode.getAddress(), firstDspNode.getPropagationPort());
            networkService.getNetwork().addNode(firstDspNode);
        }
        if (dspNodes.size() > 1) { //TODO: subscribing only to one dsp ?
            communicationService.addSender(firstDspNode.getAddress(), dspNodes.get(1).getReceivingPort());
        }

    }

    protected Node getNodeProperties(){
        return new Node(NodeType.FullNode, nodeIp, serverPort);
    }
}