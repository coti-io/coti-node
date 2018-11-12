package io.coti.nodemanager.services;

import io.coti.nodemanager.model.ActiveNode;
import io.coti.nodemanager.services.interfaces.INodeManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class InitializationService {

    @Autowired
    private INodeManagementService nodeManagementService;

    @Autowired
    private ActiveNode activeNode;


    @PostConstruct
    private void init() {
        insertActiveNodesToMemory();
    }

    private void insertActiveNodesToMemory() {
        activeNode.forEach(activeNodeData -> {
            if(activeNodeData.getNetworkNodeData() != null) {
                nodeManagementService.getAllNetworkData().addNode(activeNodeData.getNetworkNodeData());
            }
        }
    );
}

}
