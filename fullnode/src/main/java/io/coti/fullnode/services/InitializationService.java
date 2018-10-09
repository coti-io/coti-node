package io.coti.fullnode.services;

import io.coti.basenode.data.Node;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;

@Service
public class InitializationService extends BaseNodeInitializationService {
    @Autowired
    private CommunicationService communicationService;

    @PostConstruct
    public void init() {
        super.connectToNetwork();
        List<Node> dspNodes = baseNodeNetworkService.getNetwork().dspNodes;
        Collections.shuffle(dspNodes);
        super.init();

        if (dspNodes.size() > 0) {
            communicationService.addSender(dspNodes.get(0).getAddress(), dspNodes.get(0).getReceivingPort());
            communicationService.addSubscription(dspNodes.get(0).getAddress(), dspNodes.get(0).getPropagationPort());
        }
        if (dspNodes.size() > 1) {
            communicationService.addSender(dspNodes.get(0).getAddress(), dspNodes.get(0).getReceivingPort());
            communicationService.addSubscription(dspNodes.get(0).getAddress(), dspNodes.get(0).getPropagationPort());
        }
    }

    protected Node getNodeProperties(){
        return new Node(NodeType.FullNode, "localhost", "7020");
    }
}