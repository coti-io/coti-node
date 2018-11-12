package io.coti.basenode.services;

import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.Iterator;
import java.util.List;

@Slf4j
public abstract class BaseNodeNetworkService {

    protected String recoveryServerAddress;

    protected NetworkDetails networkDetails;

    private CommunicationService communicationService;


    private void initiateContext(ApplicationContext applicationContext) {
        communicationService = applicationContext.getBean(CommunicationService.class);
    }

    public NetworkDetails getNetworkDetails() {
        return networkDetails;
    }

    public void saveNetwork(NetworkDetails networkDetails) {
        this.networkDetails = networkDetails;
    }

    public String getRecoveryServerAddress() {
        return recoveryServerAddress;
    }

    public void setRecoveryServerAddress(String recoveryServerAddress) {
        this.recoveryServerAddress = recoveryServerAddress;
    }

    public void addListToSubscriptionAndNetwork(List<NetworkNodeData> nodeDataList) {
        Iterator<NetworkNodeData> nodeDataIterator = nodeDataList.iterator();
        while (nodeDataIterator.hasNext()) {
            NetworkNodeData node = nodeDataIterator.next();
            log.info("{} {} is about to be added to subscription and network", node.getNodeType(), node.getHttpFullAddress());
            networkDetails.addNode(node);
            communicationService.addSubscription(node.getPropagationFullAddress());
        }
    }

    protected void init(ApplicationContext applicationContext) {
        networkDetails = new NetworkDetails();
        initiateContext(applicationContext);
    }
}
