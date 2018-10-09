package io.coti.trustscore.services;

import io.coti.basenode.data.Node;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class InitializationService extends BaseNodeInitializationService{
    @Value("#{'${propagation.server.addresses}'.split(',')}")
    private List<String> propagationServerAddresses;
    @Autowired
    private CommunicationService communicationService;

    @PostConstruct
    public void init() {
        super.connectToNetwork();
        communicationService.initSubscriber(NodeType.TrustScoreNode);

        super.init();
    }

    @Override
    protected Node getNodeProperties() {
        return new Node(NodeType.TrustScoreNode, "localhost", "8020");

    }
}