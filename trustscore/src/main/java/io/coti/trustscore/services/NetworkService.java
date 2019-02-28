package io.coti.trustscore.services;

import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
import io.coti.basenode.services.BaseNodeNetworkService;
import io.coti.basenode.services.CommunicationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Service
@Slf4j
public class NetworkService extends BaseNodeNetworkService {

    @Value("${server.ip}")
    protected String nodeIp;
    @Value("${server.port}")
    private String serverPort;
    @Autowired
    private CommunicationService communicationService;

    @Override
    public void handleNetworkChanges(NetworkData newNetworkData) {
        log.info("New network structure received");
        NetworkNodeData zerospendNetworkNodeData = newNetworkData.getSingleNodeNetworkDataMap().get(NodeType.ZeroSpendServer);
        if (zerospendNetworkNodeData != null && zerospendNetworkNodeData != getSingleNodeData(NodeType.ZeroSpendServer)) {
            log.info("Zero spend server {} is about to be added", zerospendNetworkNodeData.getHttpFullAddress());
            recoveryServerAddress = zerospendNetworkNodeData.getHttpFullAddress();
            communicationService.addSubscription(zerospendNetworkNodeData.getPropagationFullAddress());
        }
        List<NetworkNodeData> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(newNetworkData.getMultipleNodeMaps().get(NodeType.DspNode).values(),
                getMapFromFactory(NodeType.DspNode).values()));
        if (!dspNodesToConnect.isEmpty()) {
            dspNodesToConnect.removeIf(dsp -> dsp.getAddress().equals(nodeIp) && dsp.getHttpPort().equals(serverPort));
            Collections.shuffle(dspNodesToConnect);
            addListToSubscriptionAndNetwork(dspNodesToConnect);
        }
        setNetworkData(newNetworkData);
    }


}
