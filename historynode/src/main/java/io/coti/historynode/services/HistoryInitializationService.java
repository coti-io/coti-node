package io.coti.historynode.services;

//import io.coti.basenode.communication.Channel;
//import io.coti.basenode.data.ClusterStampConsensusResult;
//import io.coti.basenode.data.ClusterStampData;
//import io.coti.basenode.data.NodeType;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.AddressData;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.CommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Consumer;

@Slf4j
@Service
public class HistoryInitializationService extends BaseNodeInitializationService {
    @Value("#{'${propagation.server.addresses}'.split(',')}")
    private List<String> propagationServerAddresses;
    @Value("${receiving.port}")
    private String receivingPort;
    @Value("${propagation.port}")
    private String propagationPort;
    @Value("${server.port}")
    private String serverPort;

//    @Autowired
//    private BaseNodeInitializationService baseNodeInitializationService;
    @Autowired
    private CommunicationService communicationService;
    @Autowired
    private ClusterStampService clusterStampService;
    @Autowired
    private HistoryTransactionService historyTransactionService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private TransactionService transactionService;

    private EnumMap<NodeType, List<Class<? extends IPropagatable>>> publisherNodeTypeToMessageTypesMap = new EnumMap<>(NodeType.class);

    @PostConstruct
    public void init() {
        super.initDB();
        super.createNetworkNodeData();
        super.getNetwork();

        publisherNodeTypeToMessageTypesMap.put(NodeType.ZeroSpendServer, Arrays.asList(TransactionData.class, AddressData.class));
        publisherNodeTypeToMessageTypesMap.put(NodeType.FinancialServer, Arrays.asList(TransactionData.class));

        communicationService.initSubscriber(NodeType.HistoryNode, publisherNodeTypeToMessageTypesMap);

        NetworkNodeData zerospendNetworkNodeData = networkService.getSingleNodeData(NodeType.ZeroSpendServer);
        if (zerospendNetworkNodeData == null) {
            log.error("No zerospend server exists in the network got from the node manager, about to exit application");
            System.exit(-1);
        }
        networkService.setRecoveryServerAddress(zerospendNetworkNodeData.getHttpFullAddress());
        communicationService.initPublisher(propagationPort, NodeType.HistoryNode);

//        HashMap<String, Consumer<Object>> classNameToReceiverHandlerMapping = new HashMap<>();
//        classNameToReceiverHandlerMapping.put(TransactionData.class.getName(), data ->
//                transactionService.handleNewTransactionFromFullNode((TransactionData) data));
//
//        classNameToReceiverHandlerMapping.put(AddressData.class.getName(), data ->
//                addressService.handleNewAddressFromFullNode((AddressData) data));
//
//        communicationService.initReceiver(receivingPort, classNameToReceiverHandlerMapping);
//        communicationService.addSender(zerospendNetworkNodeData.getReceivingFullAddress());
//        communicationService.addSubscription(zerospendNetworkNodeData.getPropagationFullAddress(), NodeType.ZeroSpendServer);
//        List<NetworkNodeData> dspNetworkNodeDataList = networkService.getMapFromFactory(NodeType.DspNode).values().stream()
//                .filter(dspNode -> !dspNode.equals(networkService.getNetworkNodeData()))
//                .collect(Collectors.toList());

//        networkService.addListToSubscription(dspNetworkNodeDataList);
        if (networkService.getSingleNodeData(NodeType.FinancialServer) != null) {
            networkService.addListToSubscription(new ArrayList<>(Arrays.asList(networkService.getSingleNodeData(NodeType.FinancialServer))));
        }

        super.init();





        initSubscriber();
    }

    @Override
    protected NetworkNodeData createNodeProperties() {
        NetworkNodeData networkNodeData = new NetworkNodeData(NodeType.HistoryNode, nodeIp, serverPort, NodeCryptoHelper.getNodeHash(), networkType);
        networkNodeData.setPropagationPort(propagationPort);
        networkNodeData.setReceivingPort(receivingPort);
        return networkNodeData;
    }

    public void initSubscriber() {
        HashMap<String, Consumer<Object>> classNameToSubscriberHandler = new HashMap<>();
//TODO: For initial compilation prior to merge
//        classNameToSubscriberHandler.put(Channel.getChannelString(ClusterStampData.class, NodeType.HistoryNode), data ->
//                clusterStampService.handleClusterStampData((ClusterStampData) data));
//        classNameToSubscriberHandler.put(Channel.getChannelString(ClusterStampConsensusResult.class, NodeType.HistoryNode), data ->
//                clusterStampService.handleClusterStampConsensusResult((ClusterStampConsensusResult) data));
//
//        communicationService.initSubscriber(propagationServerAddresses, NodeType.HistoryNode, classNameToSubscriberHandler);
    }
}
