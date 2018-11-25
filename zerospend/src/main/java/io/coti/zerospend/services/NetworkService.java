package io.coti.zerospend.services;

import io.coti.basenode.data.NetworkDetails;
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
    public void handleNetworkChanges(NetworkDetails newNetworkDetails) {
        log.info("New newNetworkDetails structure received: {}", networkDetailsService.getNetWorkSummary(newNetworkDetails));
        List<NetworkNodeData> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(
                newNetworkDetails.getDspNetworkNodesList(), networkDetailsService.getNetworkDetails().getDspNetworkNodesList()
        ));
        addListToSubscriptionAndNetwork(dspNodesToConnect);
        networkDetailsService.setNetworkDetails(newNetworkDetails);
    }


}
