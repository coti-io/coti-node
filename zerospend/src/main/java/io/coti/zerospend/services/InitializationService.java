package io.coti.zerospend.services;

import io.coti.basenode.data.DspVote;
import io.coti.basenode.data.Network;
import io.coti.basenode.data.Node;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.model.Transactions;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

@Service
public class InitializationService {
    @Value("${receiving.port}")
    private String receivingPort;
    @Value("#{'${propagation.server.addresses}'.split(',')}")
    private List<String> propagationServerAddresses;
    @Value("${propagation.port}")
    private String propagationPort;
    @Value("${node.manager.address}")
    private String nodeManagerAddress;

    @Autowired
    private CommunicationService communicationService;
    @Autowired
    private DspVoteService dspVoteService;
    @Autowired
    private BaseNodeInitializationService baseNodeInitializationService;
    @Autowired
    private TransactionCreationService transactionCreationService;
    @Autowired
    private Transactions transactions;

    @PostConstruct
    public void init() {
        connectToNodeManager();

        HashMap<String, Consumer<Object>> classNameToReceiverHandlerMapping = new HashMap<>();
        classNameToReceiverHandlerMapping.put(
                DspVote.class.getName(), data ->
                        dspVoteService.receiveDspVote((DspVote) data));
        communicationService.initReceiver(receivingPort, classNameToReceiverHandlerMapping);
        communicationService.initSubscriber(propagationServerAddresses, NodeType.ZeroSpendServer);
        communicationService.initPropagator(propagationPort);

        baseNodeInitializationService.init();

        if (transactions.isEmpty()) {
            transactionCreationService.createGenesisTransactions();
        }
    }

    private void connectToNodeManager() {
        RestTemplate restTemplate = new RestTemplate();
        String address = nodeManagerAddress + "/nodes/newNode";
        Node node = new Node(NodeType.ZeroSpendServer, "localhost", "7020");
        node.setPropagationPort("5002");
        node.setReceivingPort("5001");
        restTemplate.postForObject(address, node, String.class);
    }
}