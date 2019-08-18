package io.coti.zerospend.services;

import io.coti.basenode.crypto.NodeCryptoHelper;
import io.coti.basenode.data.DspVote;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.TransactionData;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.model.Transactions;
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

import static io.coti.basenode.constants.BaseNodeApplicationConstant.*;

@Slf4j
@Service
public class InitializationService extends BaseNodeInitializationService {

    @Value("${receiving.port}")
    private String receivingPort;
    @Value("${propagation.port}")
    private String propagationPort;
    @Value("${recovery.server.address}")
    private String recoveryServerAddress;
    @Autowired
    private ICommunicationService communicationService;
    @Autowired
    private DspVoteService dspVoteService;
    @Autowired
    private TransactionCreationService transactionCreationService;
    @Autowired
    private Transactions transactions;
    private EnumMap<NodeType, List<Class<? extends IPropagatable>>> publisherNodeTypeToMessageTypesMap = new EnumMap<>(NodeType.class);

    @PostConstruct
    public void init() {
        try {
            super.initDB();
            super.createNetworkNodeData();
            super.getNetwork();

            publisherNodeTypeToMessageTypesMap.put(NodeType.FinancialServer, Arrays.asList(TransactionData.class));

            communicationService.initSubscriber(NodeType.ZeroSpendServer, publisherNodeTypeToMessageTypesMap);

            HashMap<String, Consumer<Object>> classNameToReceiverHandlerMapping = new HashMap<>();
            classNameToReceiverHandlerMapping.put(
                    DspVote.class.getName(), data ->
                            dspVoteService.receiveDspVote((DspVote) data));
            communicationService.initReceiver(receivingPort, classNameToReceiverHandlerMapping);

            communicationService.initPublisher(propagationPort, NodeType.ZeroSpendServer);

            networkService.addListToSubscription(networkService.getMapFromFactory(NodeType.DspNode).values());
            if (networkService.getSingleNodeData(NodeType.FinancialServer) != null) {
                networkService.addListToSubscription(new ArrayList<>(Arrays.asList(networkService.getSingleNodeData(NodeType.FinancialServer))));
            }
            if (!recoveryServerAddress.isEmpty()) {
                networkService.setRecoveryServerAddress(recoveryServerAddress);
            }

            super.init();

            if (transactions.isEmpty()) {
                transactionCreationService.createGenesisTransactions();
            }
        } catch (Exception e) {
            log.error("Errors at {}", this.getClass().getSimpleName());
            log.error("{}: {}", e.getClass().getName(), e.getMessage());
            System.exit(SpringApplication.exit(applicationContext));
        }

    }

    protected NetworkNodeData createNodeProperties() {
        NetworkNodeData networkNodeData = new NetworkNodeData(NodeType.ZeroSpendServer, NODE_IP, NODE_PORT, NodeCryptoHelper.getNodeHash(), NETWORK_TYPE);
        networkNodeData.setPropagationPort(propagationPort);
        networkNodeData.setReceivingPort(receivingPort);
        return networkNodeData;
    }
}