package io.coti.zerospend.services;

import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.data.DspVote;
import io.coti.basenode.data.NetworkNode;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;
import io.coti.basenode.services.interfaces.INetworkService;
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
    @Value("${server.port}")
    private String serverPort;
    @Value("${server.ip}")
    private String nodeIp;

    @Autowired
    private CommunicationService communicationService;
    @Autowired
    private DspVoteService dspVoteService;
    @Autowired
    private TransactionCreationService transactionCreationService;
    @Autowired
    private Transactions transactions;
    @Autowired
    private INetworkService networkService;
    @Autowired
    private IPropagationSubscriber subscriber;

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
        if (networkService.getNetworkData().getDspNetworkNodes().size() > 0) {
            networkService.getNetworkData().getDspNetworkNodes().forEach(dspNode -> {
                subscriber.connectAndSubscribeToServer(dspNode.getPropagationFullAddress());
                networkService.getNetworkData().addNode(dspNode);
            });
        }
        super.init();
        if (transactions.isEmpty()) {
            transactionCreationService.createGenesisTransactions();
        }
    }

    protected NetworkNode getNodeProperties() {
        networkNode = new NetworkNode(NodeType.ZeroSpendServer, nodeIp, serverPort);
        networkNode.setPropagationPort(propagationPort);
        networkNode.setReceivingPort(receivingPort);
        return networkNode;
    }
}