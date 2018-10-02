package io.coti.dspnode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Service
public class InitializationService {
    @Value("${receiving.port}")
    private String receivingPort;
    @Value("${propagation.port}")
    private String propagationPort;
    @Value("${node.manager.address}")
    private String nodeManagerAddress;

    @Autowired
    private BaseNodeInitializationService baseNodeInitializationService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private CommunicationService communicationService;

    @PostConstruct
    public void init() {
        RegisterToNodeManager();
        Network network = getNetwork();

        HashMap<String, Consumer<Object>> classNameToReceiverHandlerMapping = new HashMap<>();
        classNameToReceiverHandlerMapping.put(TransactionData.class.getName(), data ->
                transactionService.handleNewTransactionFromFullNode((TransactionData) data));
        classNameToReceiverHandlerMapping.put(AddressData.class.getName(), data ->
                addressService.handleNewAddressFromFullNode((AddressData) data));

        communicationService.initReceiver(receivingPort, classNameToReceiverHandlerMapping);
        communicationService.initSender(
                Arrays.asList("tcp://" + network.getZerospendServer().getAddress() + ":" + network.getZerospendServer().getPropagationPort()));
        communicationService.initSubscriber(
                Arrays.asList("tcp://" + network.getZerospendServer().getAddress() + ":" + network.getZerospendServer().getPropagationPort()),
                NodeType.DspNode);
        communicationService.initPropagator(propagationPort);

        baseNodeInitializationService.init();
    }

    private void RegisterToNodeManager() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(nodeManagerAddress + "/nodes/newNode", new Node(NodeType.DspNode, "localhost", "8020"), String.class);
    }

    private Network getNetwork() {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(nodeManagerAddress + "/nodes/all", Network.class);
    }
}
