package io.coti.nodemanager.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.exceptions.CotiRunTimeException;
import io.coti.nodemanager.data.ActiveNodeData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;

import static io.coti.nodemanager.services.NodeServiceManager.*;

@Slf4j
@Service
public class InitializationService {
    @Autowired
    NodeServiceManager nodeServiceManager;
    @Value("${propagation.port}")
    private String propagationPort;
    @Autowired
    public BuildProperties buildProperties;

    @PostConstruct
    private void init() {
        try {
            nodeServiceManager.init();
            nodeIdentityService.init();
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
