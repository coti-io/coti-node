package io.coti.storagenode.services;

import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

@Service
public class InitializationService extends BaseNodeInitializationService{
    @Value("${receiving.port}")
    private String receivingPort;
    @Value("${server.port}")
    private String serverPort;
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

//    @Autowired
//    private BaseNodeInitializationService baseNodeInitializationService;

    @Autowired
    private CommunicationService communicationService;

    private EnumMap<NodeType, List<Class<? extends IPropagatable>>> publisherNodeTypeToMessageTypesMap = new EnumMap<>(NodeType.class);

    @PostConstruct
    public void init()
    {
        super.initDB();
//        super.createNetworkNodeData();
        super.getNetwork();

        HashMap<String, Consumer<Object>> classNameToReceiverHandlerMapping = new HashMap<>();
        HashMap<String, Consumer<Object>> classNameToSubscriberHandler = new HashMap<>();

        publisherNodeTypeToMessageTypesMap.put(NodeType.HistoryNode, Arrays.asList(TransactionData.class));

        // TODO implement  handler according to channels
//        classNameToSubscriberHandler.put(Channel.getChannelString(TBD.class, NodeType.HistoryNode), TBDConsumer);

        communicationService.initReceiver(receivingPort, classNameToReceiverHandlerMapping);
//        communicationService.initSender(receivingServerAddresses);
        communicationService.initSubscriber(NodeType.HistoryNode, publisherNodeTypeToMessageTypesMap); // TODO: check this
//        communicationService.initPropagator(propagationPort);

//        baseNodeInitializationService.init();
//        super.init();

    }

    @Override
    protected NetworkNodeData createNodeProperties() {
        NetworkNodeData networkNodeData = new NetworkNodeData(NodeType.StorageNode, nodeIp, serverPort, NodeCryptoHelper.getNodeHash(), networkType);
        networkNodeData.setPropagationPort(propagationPort);
        networkNodeData.setReceivingPort(receivingPort);
        return networkNodeData;
    }
}
