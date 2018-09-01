package io.coti.dspnode.services;

import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
    @Value("#{'${zerospend.receiving.address}'.split(',')}")
    private List<String> receivingServerAddresses;

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
        baseNodeInitializationService.init();

        HashMap<String, Consumer<Object>> classNameToReceiverHandlerMapping = new HashMap<>();
        classNameToReceiverHandlerMapping.put(TransactionData.class.getName(), data ->
                transactionService.handleNewTransactionFromFullNode((TransactionData) data));
        classNameToReceiverHandlerMapping.put(AddressData.class.getName(), data ->
                addressService.handleNewAddressFromFullNode((AddressData) data));

        communicationService.initReceiver(receivingPort, classNameToReceiverHandlerMapping);
        communicationService.initSender(receivingServerAddresses);
        communicationService.initSubscriber(propagationServerAddresses, NodeType.DspNode);
        communicationService.initPropagator(propagationPort);


    }
}
