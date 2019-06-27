package io.coti.storagenode.services;

import io.coti.storagenode.database.DbConnectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

import static io.coti.storagenode.services.AddressStorageService.ADDRESS_TRANSACTION_HISTORY_INDEX_NAME;
import static io.coti.storagenode.services.AddressStorageService.ADDRESS_TRANSACTION_HISTORY_OBJECT_NAME;
import static io.coti.storagenode.services.TransactionStorageService.TRANSACTION_INDEX_NAME;
import static io.coti.storagenode.services.TransactionStorageService.TRANSACTION_OBJECT_NAME;

@Service
public class InitializationService /*extends BaseNodeInitializationService*/{
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
    private DbConnectorService dbConnectorService;

//    @Autowired
//    private BaseNodeInitializationService baseNodeInitializationService;

//    @Autowired
//    private CommunicationService communicationService;

//    private EnumMap<NodeType, List<Class<? extends IPropagatable>>> publisherNodeTypeToMessageTypesMap = new EnumMap<>(NodeType.class);

    private Map<String, String> indexes;

    @PostConstruct
    public void init()
    {

        indexes = new HashMap<>();
        indexes.put(TRANSACTION_INDEX_NAME, TRANSACTION_OBJECT_NAME);
        indexes.put(ADDRESS_TRANSACTION_HISTORY_INDEX_NAME, ADDRESS_TRANSACTION_HISTORY_OBJECT_NAME);
        try {
            dbConnectorService.addIndexes(indexes, false);
            dbConnectorService.addIndexes(indexes, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//        super.initDB();
//        super.createNetworkNodeData();
//        super.getNetwork();

//        HashMap<String, Consumer<Object>> classNameToReceiverHandlerMapping = new HashMap<>();
//        HashMap<String, Consumer<Object>> classNameToSubscriberHandler = new HashMap<>();
//
//        publisherNodeTypeToMessageTypesMap.put(NodeType.HistoryNode, Arrays.asList(TransactionData.class));

        // TODO implement  handler according to channels
//        classNameToSubscriberHandler.put(Channel.getChannelString(TBD.class, NodeType.HistoryNode), TBDConsumer);

//        communicationService.initReceiver(receivingPort, classNameToReceiverHandlerMapping);
//        communicationService.initSender(receivingServerAddresses);
//        communicationService.initSubscriber(NodeType.HistoryNode, publisherNodeTypeToMessageTypesMap); // TODO: check this
//        communicationService.initPropagator(propagationPort);

//        baseNodeInitializationService.init();
//        super.init();

//    }

//    @Override
//    protected NetworkNodeData createNodeProperties() {
//        NetworkNodeData networkNodeData = new NetworkNodeData(NodeType.StorageNode, nodeIp, serverPort, NodeCryptoHelper.getNodeHash(), networkType);
//        networkNodeData.setPropagationPort(propagationPort);
//        networkNodeData.setReceivingPort(receivingPort);
//        return networkNodeData;
//    }

}
