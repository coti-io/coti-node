package io.coti.zerospend.services;

import io.coti.basenode.data.DspVote;
import io.coti.basenode.data.Node;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.function.Consumer;

@Service
public class InitializationService extends BaseNodeInitializationService {
    @Value("${receiving.port}")
    private String receivingPort;
    @Value("${propagation.port}")
    private String propagationPort;

    @Autowired
    private CommunicationService communicationService;
    @Autowired
    private DspVoteService dspVoteService;
    @Autowired
    private TransactionCreationService transactionCreationService;
    @Autowired
    private Transactions transactions;

    @PostConstruct
    public void init() {
        super.connectToNetwork();
        HashMap<String, Consumer<Object>> classNameToReceiverHandlerMapping = new HashMap<>();
        classNameToReceiverHandlerMapping.put(
                DspVote.class.getName(), data ->
                        dspVoteService.receiveDspVote((DspVote) data));
        communicationService.initReceiver(receivingPort, classNameToReceiverHandlerMapping);
        communicationService.initSubscriber(NodeType.ZeroSpendServer);
        communicationService.initPropagator(propagationPort);

        baseNodeNetworkService.getNetwork().dspNodes.forEach(dspNode -> communicationService.addSubscription(dspNode.getAddress(), dspNode.getPropagationPort()));

        super.init();
        if (transactions.isEmpty()) {
            transactionCreationService.createGenesisTransactions();
        }
    }

    protected Node getNodeProperties() {
        node = new Node(NodeType.ZeroSpendServer, "localhost", "7020");
        node.setPropagationPort(propagationPort);
        node.setReceivingPort(receivingPort);
        return node;
    }
}