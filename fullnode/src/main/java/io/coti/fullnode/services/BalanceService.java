package io.coti.fullnode.services;

import io.coti.basenode.data.ClusterStampData;
import io.coti.basenode.data.Hash;
import io.coti.basenode.services.BaseNodeBalanceService;
import io.coti.basenode.services.interfaces.IClusterStampService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BalanceService extends BaseNodeBalanceService {
    @Autowired
    private WebSocketSender webSocketSender;

    @Autowired
    private IClusterStampService clusterStampService;

    @Override
    public void continueHandleBalanceChanges(Hash addressHash) {
        webSocketSender.notifyBalanceChange(addressHash, balanceMap.get(addressHash), preBalanceMap.get(addressHash));
    }

    @Override
    protected void loadBalanceFromClusterStamp(ClusterStampData lastClusterStampData) {
        clusterStampService.loadBalanceFromClusterStamp(lastClusterStampData);
    }

}
