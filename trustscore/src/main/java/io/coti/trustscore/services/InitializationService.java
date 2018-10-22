package io.coti.trustscore.services;

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
public class InitializationService extends BaseNodeInitializationService{

    @Autowired
    private CommunicationService communicationService;
    @Autowired
    private INetworkService networkService;

    @Value("${server.port}")
    private String serverPort;
    @Value("${server.ip}")
    private String nodeIp;

    @Autowired
    private IPropagationSubscriber subscriber;

    @PostConstruct
    public void init() {
        super.connectToNetwork();
        communicationService.initSubscriber(NodeType.TrustScoreNode);
        List<Node> dspNodes = this.networkService.getNetwork().dspNodes;
        Collections.shuffle(dspNodes);
        Node zerospendNode = this.networkService.getNetwork().getZerospendServer();
        if(zerospendNode != null ) {
            networkService.setRecoveryServerAddress(zerospendNode.getHttpFullAddress());
        }
        if(dspNodes.size() > 0){
                dspNodes.forEach(dspnode -> subscriber.connectAndSubscribeToServer(dspnode.getPropagationFullAddress()));//communicationService.addSubscription(dspnode.getAddress(), dspnode.getPropagationPort()));
        }
        if(zerospendNode != null ) {
            subscriber.connectAndSubscribeToServer(zerospendNode.getPropagationFullAddress());//communicationService.addSubscription(zerospendNode.getAddress(), zerospendNode.getPropagationPort());
        }
        super.init();
//        subscriber.subscribeAll(this.networkService.getNetwork().getNodeManagerPropagationAddress());
//        subscriber.subscribeAll(zerospendNode.getPropagationFullAddress());
//        if(dspNodes.size() > 0){
//            dspNodes.forEach(dspnode ->  subscriber.subscribeAll(dspnode.getPropagationFullAddress()));
//        }

    }

    @Override
    protected Node getNodeProperties() {
        return new Node(NodeType.TrustScoreNode, nodeIp, serverPort);

    }
}