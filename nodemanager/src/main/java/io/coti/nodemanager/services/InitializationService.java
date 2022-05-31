package io.coti.nodemanager.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.database.interfaces.IDatabaseConnector;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.basenode.services.interfaces.*;
import io.coti.nodemanager.data.ActiveNodeData;
import io.coti.nodemanager.model.ActiveNodes;
import io.coti.nodemanager.model.NodeDailyActivities;
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
import java.util.HashMap;

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
    private ApplicationContext applicationContext;
    @Autowired
    private BuildProperties buildProperties;
    @Autowired
    private NodeDailyActivities nodeDailyActivities;

    @PostConstruct
    private void init() {
        try {
            log.info("Application name: {}, version: {}", buildProperties.getName(), buildProperties.getVersion());
            databaseConnector.init();
            awsService.init();
            dbRecoveryService.init();
            networkService.init();
            setNetworkLastKnownNodes();
            insertActiveNodesToMemory();
            nodeManagementService.init();
            communicationService.initPublisher(propagationPort, NodeType.NodeManager);
            healthCheckService.init();
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

    private void setNetworkLastKnownNodes() {
        HashMap<Hash, NetworkNodeData> activeHistoryNodes = new HashMap<>();
        nodeDailyActivities.forEach(consumer ->
        {
            ActiveNodeData activeNodeData = activeNodes.getByHash(consumer.getHash());
            if (activeNodeData != null) {
                NetworkNodeData networkNodeData = activeNodeData.getNetworkNodeData();
                activeHistoryNodes.putIfAbsent(consumer.getHash(), networkNodeData);
            }
        });

        networkService.setNetworkLastKnownNodeMap(activeHistoryNodes);
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
