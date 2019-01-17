package io.coti.nodemanager.services;

import io.coti.basenode.services.interfaces.INetworkDetailsService;
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

    @Autowired
    private INetworkDetailsService networkDetailsService;


    @PostConstruct
    private void init() {
        insertActiveNodesToMemory();
    }

    private void insertActiveNodesToMemory() {
        activeNode.forEach(activeNodeData -> {
            if(activeNodeData.getNetworkNodeData() != null) {
                networkDetailsService.addNode(activeNodeData.getNetworkNodeData());
            }
        }
    );
}

}
