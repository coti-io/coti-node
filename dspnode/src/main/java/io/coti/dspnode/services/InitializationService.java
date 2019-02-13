package io.coti.dspnode.services;

import io.coti.basenode.communication.Channel;
import io.coti.basenode.data.*;
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
    @Autowired
    private ClusterStampService clusterStampService;

    @PostConstruct
    public void init() {


        initReceiver();
        communicationService.initSender(receivingServerAddresses);
        initSubscriber();
        communicationService.initPropagator(propagationPort);

        baseNodeInitializationService.init();

    }

    public void initReceiver(){
        HashMap<String, Consumer<Object>> classNameToReceiverHandlerMapping = new HashMap<>();
        classNameToReceiverHandlerMapping.put(TransactionData.class.getName(), data ->
                transactionService.handleNewTransactionFromFullNode((TransactionData) data));
        classNameToReceiverHandlerMapping.put(AddressData.class.getName(), data ->
                addressService.handleNewAddressFromFullNode((AddressData) data));
        classNameToReceiverHandlerMapping.put(FullNodeReadyForClusterStampData.class.getName(), data ->
                clusterStampService.handleFullNodeReadyForClusterStampMessage((FullNodeReadyForClusterStampData) data));

        communicationService.initReceiver(receivingPort, classNameToReceiverHandlerMapping);
    }

    public void initSubscriber(){
        HashMap<String, Consumer<Object>> classNameToSubscriberHandler = new HashMap<>();
        classNameToSubscriberHandler.put(Channel.getChannelString(ClusterStampPreparationData.class, NodeType.DspNode), data ->
                clusterStampService.prepareForClusterStamp((ClusterStampPreparationData) data));
        classNameToSubscriberHandler.put(Channel.getChannelString(ClusterStampData.class, NodeType.DspNode), data ->
                clusterStampService.newClusterStamp((ClusterStampData) data));
        classNameToSubscriberHandler.put(Channel.getChannelString(ClusterStampConsensusResult.class, NodeType.DspNode), data ->
                clusterStampService.newClusterStampConsensusResult((ClusterStampConsensusResult) data));

        communicationService.initSubscriber(propagationServerAddresses, NodeType.DspNode, classNameToSubscriberHandler);
    }
}
