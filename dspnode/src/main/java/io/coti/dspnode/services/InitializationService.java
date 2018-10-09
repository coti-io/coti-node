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
public class InitializationService extends BaseNodeInitializationService{
    @Value("${receiving.port}")
    private String receivingPort;
    @Value("${propagation.port}")
    private String propagationPort;
    @Value("${node.manager.address}")
    private String nodeManagerAddress;

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private CommunicationService communicationService;

    @PostConstruct
    public void init() {
        super.connectToNetwork();
        HashMap<String, Consumer<Object>> classNameToReceiverHandlerMapping = new HashMap<>();
        classNameToReceiverHandlerMapping.put(TransactionData.class.getName(), data ->
                transactionService.handleNewTransactionFromFullNode((TransactionData) data));
        classNameToReceiverHandlerMapping.put(AddressData.class.getName(), data ->
                addressService.handleNewAddressFromFullNode((AddressData) data));

        communicationService.initReceiver(receivingPort, classNameToReceiverHandlerMapping);
        communicationService.initSubscriber(NodeType.DspNode);
        communicationService.initPropagator(propagationPort);

        super.init();
    }

    @Override
    protected Node getNodeProperties() {
        Node node = new Node(NodeType.DspNode, "localhost", "8020");
        node.setPropagationPort(propagationPort);
        node.setReceivingPort(receivingPort);
        return node;
    }
}
