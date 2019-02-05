package io.coti.nodemanager.services;

import io.coti.basenode.database.Interfaces.IDatabaseConnector;
import io.coti.basenode.services.interfaces.INetworkService;
import io.coti.nodemanager.model.ActiveNodes;
import io.coti.nodemanager.services.interfaces.INodeManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class InitializationService {

    @Autowired
    private INodeManagementService nodeManagementService;
    @Autowired
    private ActiveNodes activeNodes;
    @Autowired
    private INetworkService networkService;
    @Autowired
    private IDatabaseConnector databaseConnector;


    @PostConstruct
    private void init() {
        databaseConnector.init();
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

}
