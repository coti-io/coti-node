package io.coti.fullnode.services;

import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.services.BaseNodeNetworkService;
import io.coti.basenode.services.CommunicationService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Service
@Slf4j
public class NetworkService extends BaseNodeNetworkService implements INetworkService {

    @Autowired
    private CommunicationService communicationService;
    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    protected void init() {
        super.init(applicationContext);
    }

    @Override
    public void handleNetworkChanges(NetworkDetails newNetworkDetails) {
        log.info("New newNetworkDetails structure received: {}", newNetworkDetails.getNetWorkSummary());
        this.networkDetails.getDspNetworkNodesList().forEach(dsp -> {
            if (!newNetworkDetails.getDspNetworkNodesList().contains(dsp)) {
                log.info("dsp {} is about disconnect from subscribing and receiving ", dsp.getHttpFullAddress());
                communicationService.removeSubscription(dsp.getPropagationFullAddress(), dsp.getNodeType());
                communicationService.removeSender(dsp.getReceivingFullAddress(), dsp.getNodeType());
            }
        });
        List<NetworkNodeData> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(newNetworkDetails.getDspNetworkNodesList(),
                this.networkDetails.getDspNetworkNodesList()));

        List<NetworkNodeData> twoDspNodes = new LinkedList<>();
        Collections.shuffle(dspNodesToConnect);
        for(int i = 0 ; i < dspNodesToConnect.size() && i < 2  ; i++){
            if(i == 0 && recoveryServerAddress == null ){
                recoveryServerAddress = dspNodesToConnect.get(0).getHttpFullAddress();
            }
            twoDspNodes.add(dspNodesToConnect.get(i));
        }
        addListToSubscriptionAndNetwork(twoDspNodes);
        twoDspNodes.forEach(node -> communicationService.addSender(node.getReceivingFullAddress()));
        saveNetwork(newNetworkDetails);

    }

}
