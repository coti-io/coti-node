package io.coti.zerospend.services;

import io.coti.basenode.data.NetworkDetails;
import io.coti.basenode.data.NetworkNodeData;
import io.coti.basenode.services.BaseNodeNetworkService;
import io.coti.basenode.services.interfaces.INetworkService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class NetworkService extends BaseNodeNetworkService implements INetworkService {

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    protected void init() {
        super.init(applicationContext);
    }

    @Override
    public void handleNetworkChanges(NetworkDetails newNetworkDetails) {
        log.info("New newNetworkDetails structure received: {}", newNetworkDetails.getNetWorkSummary());
        List<NetworkNodeData> dspNodesToConnect = new ArrayList<>(CollectionUtils.subtract(
                newNetworkDetails.getDspNetworkNodesList(), this.networkDetails.getDspNetworkNodesList()
        ));
        addListToSubscriptionAndNetwork(dspNodesToConnect);
        saveNetwork(newNetworkDetails);
    }


}
