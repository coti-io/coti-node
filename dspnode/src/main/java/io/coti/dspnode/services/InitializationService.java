package io.coti.dspnode.services;

import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.services.BaseNodeInitializationService;
import io.coti.basenode.services.BaseNodeMonitorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.coti.dspnode.services.NodeServiceManager.*;

@Slf4j
@Service
@Primary
public class InitializationService extends BaseNodeInitializationService {

    private final EnumMap<NodeType, List<Class<? extends IPropagatable>>> publisherNodeTypeToMessageTypesMap = new EnumMap<>(NodeType.class);
    @Value("${receiving.port}")
    private String receivingPort;
    @Value("${propagation.port}")
    private String propagationPort;
    @Value("${server.port}")
    private String serverPort;

    @PostConstruct
    @Override
    public void init() {
        try {
            super.init();
            super.initDB();
            super.createNetworkNodeData();
            super.getNetwork();

            publisherNodeTypeToMessageTypesMap.put(NodeType.ZeroSpendServer, Arrays.asList(TransactionData.class, DspConsensusResult.class, TransactionsStateData.class));
            publisherNodeTypeToMessageTypesMap.put(NodeType.FinancialServer, Collections.singletonList(TransactionData.class));

            communicationService.initSubscriber(NodeType.DspNode, publisherNodeTypeToMessageTypesMap);
            NetworkNodeData zerospendNetworkNodeData = networkService.getSingleNodeData(NodeType.ZeroSpendServer);
            if (zerospendNetworkNodeData == null) {
                log.error("No zerospend server exists in the network got from the node manager, about to exit application");
                System.exit(SpringApplication.exit(applicationContext));
            }
            networkService.setRecoveryServer(zerospendNetworkNodeData);
            communicationService.initPublisher(propagationPort, NodeType.DspNode);

            HashMap<String, Consumer<IPropagatable>> classNameToReceiverHandlerMapping = new HashMap<>();
            classNameToReceiverHandlerMapping.put(TransactionData.class.getName(), data ->
                    transactionService.handleNewTransactionFromFullNode((TransactionData) data));
            classNameToReceiverHandlerMapping.put(AddressData.class.getName(), data ->
                    addressService.handleNewAddressFromFullNode((AddressData) data));
            classNameToReceiverHandlerMapping.put(NodeResendDcrData.class.getName(), data ->
                    dspVoteService.handleDspConsensusResultResend((NodeResendDcrData) data));
            communicationService.initReceiver(receivingPort, classNameToReceiverHandlerMapping);

            communicationService.addSender(zerospendNetworkNodeData.getReceivingFullAddress(), NodeType.ZeroSpendServer);
            communicationService.addSubscription(zerospendNetworkNodeData.getPropagationFullAddress(), NodeType.ZeroSpendServer);
            List<NetworkNodeData> dspNetworkNodeDataList = networkService.getMapFromFactory(NodeType.DspNode).values().stream()
                    .filter(dspNode -> !dspNode.equals(networkService.getNetworkNodeData()))
                    .collect(Collectors.toList());
            networkService.addListToSubscription(dspNetworkNodeDataList);
            if (networkService.getSingleNodeData(NodeType.FinancialServer) != null) {
                networkService.addListToSubscription(new ArrayList<>(Collections.singletonList(networkService.getSingleNodeData(NodeType.FinancialServer))));
            }

            super.initServices();
            zeroMQReceiver.initReceiverHandler();
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
        NetworkNodeData networkNodeData = new NetworkNodeData(NodeType.DspNode, version, nodeIp, serverPort, nodeIdentityService.getNodeHash(), networkType, BaseNodeMonitorService.HealthState.NORMAL);
        networkNodeData.setPropagationPort(propagationPort);
        networkNodeData.setReceivingPort(receivingPort);
        return networkNodeData;
    }

}
