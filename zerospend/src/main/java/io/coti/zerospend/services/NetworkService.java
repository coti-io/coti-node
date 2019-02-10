package io.coti.zerospend.services;

import io.coti.basenode.data.NetworkData;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.data.NodeType;
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
        log.info("New newNetworkDetails structure received: {}");
        List<NetworkNodeData> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(
                newNetworkData.getMultipleNodeMaps().get(NodeType.DspNode).values(), getMapFromFactory(NodeType.DspNode).values()
        ));
        addListToSubscriptionAndNetwork(dspNodesToConnect);
        setNetworkData(newNetworkData);
    }


}
