package io.coti.dspnode.services;

import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.services.BaseNodeNetworkService;
import io.coti.basenode.services.CommunicationService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class NetworkService extends BaseNodeNetworkService implements INetworkService {

    @Value("${public.ip}")
    protected String nodeIp;
    @Value("${server.port}")
    private String serverPort;

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
        NetworkNodeData zerospendNetworkNodeData = newNetworkDetails.getZerospendServer();
        List<NetworkNodeData> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(newNetworkDetails.getDspNetworkNodesList()
                , this.networkDetails.getDspNetworkNodesList()));
        dspNodesToConnect.removeIf(dsp -> dsp.getAddress().equals(nodeIp) && dsp.getHttpPort().equals(serverPort));
        if (dspNodesToConnect.size() > 0) {
            Collections.shuffle(dspNodesToConnect);
            addListToSubscriptionAndNetwork(dspNodesToConnect);
        }
        if (zerospendNetworkNodeData != null && recoveryServerAddress.isEmpty()) {
            log.info("Zero spend server {} is about to be added", zerospendNetworkNodeData.getHttpFullAddress());
            recoveryServerAddress = zerospendNetworkNodeData.getHttpFullAddress();
            communicationService.addSender(zerospendNetworkNodeData.getReceivingFullAddress());
            communicationService.addSubscription(zerospendNetworkNodeData.getPropagationFullAddress());
        }
        saveNetwork(newNetworkDetails);
    }


}
