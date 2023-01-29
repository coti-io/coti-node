package io.coti.zerospend.services;

import io.coti.basenode.data.*;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.services.BaseNodeInitializationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.function.Consumer;

import static io.coti.zerospend.services.NodeServiceManager.*;

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
    @Value("${recovery.server.hash:}")
    private String recoveryServerHash;

    @PostConstruct
    @Override
    public void init() {
        try {
            super.init();
            super.initDB();
            super.createNetworkNodeData();
            super.getNetwork();

            publisherNodeTypeToMessageTypesMap.put(NodeType.FinancialServer, Collections.singletonList(TransactionData.class));

            communicationService.initSubscriber(NodeType.ZeroSpendServer, publisherNodeTypeToMessageTypesMap);

            HashMap<String, Consumer<IPropagatable>> classNameToReceiverHandlerMapping = new HashMap<>();
            classNameToReceiverHandlerMapping.put(TransactionDspVote.class.getName(), data ->
                    dspVoteService.receiveDspVote((TransactionDspVote) data));
            classNameToReceiverHandlerMapping.put(NodeResendDcrData.class.getName(), data ->
                    dspVoteService.handleDspConsensusResultResend((NodeResendDcrData) data));
            communicationService.initReceiver(receivingPort, classNameToReceiverHandlerMapping);
            communicationService.initPublisher(propagationPort, NodeType.ZeroSpendServer);

            networkService.addListToSubscription(networkService.getMapFromFactory(NodeType.DspNode).values());
            if (networkService.getSingleNodeData(NodeType.FinancialServer) != null) {
                networkService.addListToSubscription(new ArrayList<>(Collections.singletonList(networkService.getSingleNodeData(NodeType.FinancialServer))));
            }
            updateRecoveryServer();

            super.initServices();
            zeroMQReceiver.initReceiverHandler();

            transactionCreationService.init();
            if (transactions.isEmpty()) {
                transactionCreationService.createGenesisTransactions();
            }
            sourceStarvationService.init();
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

    private void updateRecoveryServer() {
        if (!recoveryServerHash.isEmpty()) {
            Hash nodeHash = new Hash(recoveryServerHash);
            if (nodeHash.getBytes().length == 0) {
                log.error("Recovery server node was not updated due to illegal node hash string.");
                return;
            }
            NetworkNodeData recoveryServer = networkService.getNetworkLastKnownNodeMap().get(nodeHash);
            if (recoveryServer != null) {
                networkService.setRecoveryServer(recoveryServer);
            } else {
                log.error("Recovery server node was not found in last known nodes map.");
            }
        }
    }

    protected NetworkNodeData createNodeProperties() {
        NetworkNodeData networkNodeData = new NetworkNodeData(NodeType.ZeroSpendServer, version, nodeIp, serverPort, nodeIdentityService.getNodeHash(), networkType, monitorService.getLastTotalHealthState());
        networkNodeData.setPropagationPort(propagationPort);
        networkNodeData.setReceivingPort(receivingPort);
        return networkNodeData;
    }
}
