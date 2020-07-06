package io.coti.nodemanager.services;

import io.coti.basenode.communication.interfaces.IPropagationSubscriber;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.messages.VoteMessageData;
import io.coti.basenode.data.messages.StateMessageData;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.services.interfaces.*;
import io.coti.nodemanager.model.ActiveNodes;
import io.coti.nodemanager.services.interfaces.IHealthCheckService;
import io.coti.nodemanager.services.interfaces.INodeManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

@Slf4j
@Service
public class InitializationService {

    @Value("${propagation.port}")
    private String propagationPort;
    @Autowired
    private ActiveNodes activeNodes;
    @Autowired
    private INetworkService networkService;
    @Autowired
    private IDatabaseConnector databaseConnector;
    @Autowired
    private IAwsService awsService;
    @Autowired
    private IDBRecoveryService dbRecoveryService;
    @Autowired
    private IShutDownService shutDownService;
    @Autowired
    private IHealthCheckService healthCheckService;
    @Autowired
    private INodeManagementService nodeManagementService;
    @Autowired
    private ICommunicationService communicationService;
    @Autowired
    private IStateMessageService stateMessageService;
    @Autowired
    private IVoteService generalVoteService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private BuildProperties buildProperties;
    @Autowired
    private IPropagationSubscriber propagationSubscriber;
    private EnumMap<NodeType, List<Class<? extends IPropagatable>>> publisherNodeTypeToMessageTypesMap = new EnumMap<>(NodeType.class);

    @PostConstruct
    private void init() {
        try {
            log.info("Application name: {}, version: {}", buildProperties.getName(), buildProperties.getVersion());
            databaseConnector.init();
            awsService.init();
            dbRecoveryService.init();
            networkService.init();

            publisherNodeTypeToMessageTypesMap.put(NodeType.ZeroSpendServer, Arrays.asList(StateMessageData.class, VoteMessageData.class));
            publisherNodeTypeToMessageTypesMap.put(NodeType.DspNode, Arrays.asList(StateMessageData.class, VoteMessageData.class));
            publisherNodeTypeToMessageTypesMap.put(NodeType.HistoryNode, Collections.singletonList(VoteMessageData.class));

            communicationService.initSubscriber(NodeType.NodeManager, publisherNodeTypeToMessageTypesMap);

            insertActiveNodesToMemory();
            nodeManagementService.init();
            propagationSubscriber.startListening();
            propagationSubscriber.initPropagationHandler();
            communicationService.initPublisher(propagationPort, NodeType.NodeManager);
            healthCheckService.init();
            stateMessageService.init();
            generalVoteService.init();
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

    private void insertActiveNodesToMemory() {
        activeNodes.forEach(activeNodeData -> {
                    if (activeNodeData.getNetworkNodeData() != null) {
                        networkService.addNode(activeNodeData.getNetworkNodeData());
                    }
                }
        );
    }

    @PreDestroy
    public void shutdown() {
        shutDownService.shutdown();
    }

}
