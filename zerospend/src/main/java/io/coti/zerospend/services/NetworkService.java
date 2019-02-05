package io.coti.zerospend.services;

import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.services.BaseNodeNetworkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class NetworkService extends BaseNodeNetworkService {

    @Override
    public void handleNetworkChanges(NetworkData newNetworkData) {
        log.info("New newNetworkDetails structure received: {}", networkDetailsService.getNetworkSummary(newNetworkData));
        List<NetworkNodeData> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(
                newNetworkData.getDspNetworkNodesMap().values(), networkDetailsService.getNetworkData().getDspNetworkNodesMap().values()
        ));
        addListToSubscriptionAndNetwork(dspNodesToConnect);
        networkDetailsService.setNetworkData(newNetworkData);
    }


}
