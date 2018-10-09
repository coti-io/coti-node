package io.coti.fullnode.services;

import io.coti.basenode.data.Network;
import io.coti.basenode.data.Node;
import io.coti.basenode.services.BaseNodeNetworkService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class NetworkService extends BaseNodeNetworkService {

    List<Node> currentlyConnectedDsps;

    @Override
    public void connectToNetwork(Network network){
        if(currentlyConnectedDsps.size() < 2){
            List<Node> dspNodesToConnect = new ArrayList<>(CollectionUtils.intersection(network.dspNodes, currentlyConnectedDsps));
            Collections.shuffle(dspNodesToConnect);
        }
    }
}
