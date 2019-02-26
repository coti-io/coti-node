package io.coti.storagenode.services;

import io.coti.basenode.communication.Channel;
import io.coti.basenode.data.NodeType;
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
    @Value("#{'${nodemanager.receiving.address}'.split(',')}")
    private List<String> receivingServerAddresses;
    @Value("#{'${propagation.server.addresses}'.split(',')}")
    private List<String> propagationServerAddresses;
    @Value("${propagation.port}")
    private String propagationPort;




    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AddressTransactionsHistoryService addressTransactionsHistoryService;

    @Autowired
    private BaseNodeInitializationService baseNodeInitializationService;

    @Autowired
    private CommunicationService communicationService;

    @PostConstruct
    public void init()
    {
        HashMap<String, Consumer<Object>> classNameToReceiverHandlerMapping = new HashMap<>();
        HashMap<String, Consumer<Object>> classNameToSubscriberHandler = new HashMap<>();

        // TODO implement  handler according to channels
//        classNameToSubscriberHandler.put(Channel.getChannelString(TBD.class, NodeType.HistoryNode), TBDConsumer);

        communicationService.initReceiver(receivingPort, classNameToReceiverHandlerMapping);
        communicationService.initSender(receivingServerAddresses);
        communicationService.initSubscriber(propagationServerAddresses, NodeType.HistoryNode, classNameToSubscriberHandler);
        communicationService.initPropagator(propagationPort);

        baseNodeInitializationService.init();

    }
}
