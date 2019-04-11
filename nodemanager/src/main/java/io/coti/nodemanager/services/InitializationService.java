package io.coti.nodemanager.services;

import io.coti.basenode.database.Interfaces.IDatabaseConnector;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.basenode.services.interfaces.IShutDownService;
import io.coti.nodemanager.model.ActiveNodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class InitializationService {

    @Autowired
    private ActiveNodes activeNodes;
    @Autowired
    private INetworkService networkService;
    @Autowired
    private IDatabaseConnector databaseConnector;
    @Autowired
    private IShutDownService shutDownService;

    @PostConstruct
    private void init() {
        databaseConnector.init();
        networkService.init();
        insertActiveNodesToMemory();
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
