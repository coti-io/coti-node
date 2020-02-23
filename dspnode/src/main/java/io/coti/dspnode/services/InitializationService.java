package io.coti.dspnode.services;

import io.coti.basenode.communication.interfaces.IReceiver;
import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.interfaces.ICommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InitializationService extends BaseNodeInitializationService {

    @Value("${receiving.port}")
    private String receivingPort;
    @Value("${propagation.port}")
    private String propagationPort;
    @Value("${server.port}")
    private String serverPort;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private AddressService addressService;
    @Autowired
    private ICommunicationService communicationService;
    @Autowired
    private IReceiver messageReceiver;
    private EnumMap<NodeType, List<Class<? extends IPropagatable>>> publisherNodeTypeToMessageTypesMap = new EnumMap<>(NodeType.class);

    @PostConstruct
    @Override
    public void init() {
        try {
            super.init();
            super.initDB();
            super.createNetworkNodeData();
            super.getNetwork();

            publisherNodeTypeToMessageTypesMap.put(NodeType.ZeroSpendServer, Arrays.asList(TransactionData.class, DspConsensusResult.class));
            publisherNodeTypeToMessageTypesMap.put(NodeType.FinancialServer, Arrays.asList(TransactionData.class));

            communicationService.initSubscriber(NodeType.DspNode, publisherNodeTypeToMessageTypesMap);
            NetworkNodeData zerospendNetworkNodeData = networkService.getSingleNodeData(NodeType.ZeroSpendServer);
            if (zerospendNetworkNodeData == null) {
                log.error("No zerospend server exists in the network got from the node manager, about to exit application");
                System.exit(SpringApplication.exit(applicationContext));
            }
            networkService.setRecoveryServerAddress(zerospendNetworkNodeData.getHttpFullAddress());
            communicationService.initPublisher(propagationPort, NodeType.DspNode);
            HashMap<String, Consumer<Object>> classNameToReceiverHandlerMapping = new HashMap<>();
            classNameToReceiverHandlerMapping.put(TransactionData.class.getName(), data ->
                    transactionService.handleNewTransactionFromFullNode((TransactionData) data));

            classNameToReceiverHandlerMapping.put(AddressData.class.getName(), data ->
                    addressService.handleNewAddressFromFullNode((AddressData) data));

            communicationService.initReceiver(receivingPort, classNameToReceiverHandlerMapping);
            communicationService.addSender(zerospendNetworkNodeData.getReceivingFullAddress());
            communicationService.addSubscription(zerospendNetworkNodeData.getPropagationFullAddress(), NodeType.ZeroSpendServer);
            List<NetworkNodeData> dspNetworkNodeDataList = networkService.getMapFromFactory(NodeType.DspNode).values().stream()
                    .filter(dspNode -> !dspNode.equals(networkService.getNetworkNodeData()))
                    .collect(Collectors.toList());
            networkService.addListToSubscription(dspNetworkNodeDataList);
            if (networkService.getSingleNodeData(NodeType.FinancialServer) != null) {
                networkService.addListToSubscription(new ArrayList<>(Arrays.asList(networkService.getSingleNodeData(NodeType.FinancialServer))));
            }

            super.initServices();
            messageReceiver.initReceiverHandler();
        } catch (CotiRunTimeException e) {
            log.error("Errors at {}", this.getClass().getSimpleName());
            e.logMessage();
            System.exit(SpringApplication.exit(applicationContext));
        } catch (Exception e) {
            log.error("Errors at {}", this.getClass().getSimpleName());
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            System.exit(SpringApplication.exit(applicationContext));
        }
    }

    @Override
    protected NetworkNodeData createNodeProperties() {
        NetworkNodeData networkNodeData = new NetworkNodeData(NodeType.DspNode, nodeIp, serverPort, NodeCryptoHelper.getNodeHash(), networkType);
        networkNodeData.setPropagationPort(propagationPort);
        networkNodeData.setReceivingPort(receivingPort);
        return networkNodeData;
    }

}
