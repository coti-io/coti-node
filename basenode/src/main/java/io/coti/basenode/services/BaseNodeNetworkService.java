package io.coti.basenode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.services.interfaces.INetworkDetailsService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.Iterator;


@Slf4j
@Service
public class BaseNodeNetworkService implements INetworkService {

    protected String recoveryServerAddress;

    @Autowired
    protected INetworkDetailsService networkDetailsService;
    @Autowired
    private CommunicationService communicationService;

    public void handleNetworkChanges(NetworkDetails networkDetails) {
    }

    public String getRecoveryServerAddress() {
        return recoveryServerAddress;
    }

    public void setRecoveryServerAddress(String recoveryServerAddress) {
        this.recoveryServerAddress = recoveryServerAddress;
    }

    public void addListToSubscriptionAndNetwork(Collection< NetworkNodeData> nodeDataList) {
        Iterator<NetworkNodeData> nodeDataIterator = nodeDataList.iterator();
        while (nodeDataIterator.hasNext()) {
            NetworkNodeData node = nodeDataIterator.next();
            log.info("{} {} is about to be added to subscription and network", node.getNodeType(), node.getHttpFullAddress());
            networkDetailsService.addNode(node);
            communicationService.addSubscription(node.getPropagationFullAddress());
        }
    }

    @PreDestroy
    private void shutdown() {
        log.error("Shutdown All Resources");
    }



}
