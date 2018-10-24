package io.coti.fullnode.services;

import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
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

    @Autowired
    private IPropagationSubscriber subscriber;
    @PostConstruct
    public void init() {
        super.connectToNetwork();
        communicationService.initSubscriber(NodeType.FullNode);
        List<Node> dspNodes = this.networkService.getNetwork().dspNodes;
        Collections.shuffle(dspNodes);
        Node firstDspNode = null;
        if (dspNodes.size() > 0) {
            firstDspNode = dspNodes.get(0);
            networkService.setRecoveryServerAddress(firstDspNode.getHttpFullAddress());
        }

        if (firstDspNode != null) {
            communicationService.addSender(firstDspNode.getReceivingFullAddress());
            subscriber.connectAndSubscribeToServer(firstDspNode.getPropagationFullAddress());
            networkService.getNetwork().addNode(firstDspNode);
        }
        if (dspNodes.size() > 1) { //TODO: subscribing only to one dsp ?
            communicationService.addSender(dspNodes.get(1).getReceivingFullAddress());
         //   subscriber.connectAndSubscribeToServer(dspNodes.get(1).getPropagationFullAddress());
        }
        super.init();
    }

    protected Node getNodeProperties(){
        return new Node(NodeType.FullNode, nodeIp, serverPort);
    }
}