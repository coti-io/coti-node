package io.coti.historynode.services;

import io.coti.basenode.data.messages.ExecuteClusterStampStateMessageData;
import io.coti.basenode.data.messages.InitiateClusterStampStateMessageData;
import io.coti.basenode.data.messages.StateMessageData;
import io.coti.basenode.services.BaseNodeClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    @Override
    public void clusterStampInitiate(StateMessageData stateMessage, InitiateClusterStampStateMessageData initiateClusterStampStateMessageData) {
        clusterStampInitiateTimestamp = stateMessage.getCreateTime();
    }

    @Override
    public void clusterStampExecute(ExecuteClusterStampStateMessageData executeClusterStampStateMessageData) {
        if (lastConfirmedIndexForClusterStamp != executeClusterStampStateMessageData.getLastIndex()) {
            log.error("Incorrect index in the CLUSTER_STAMP_EXECUTE message {} {}", lastConfirmedIndexForClusterStamp, executeClusterStampStateMessageData.getLastIndex());
            return;
        }
        super.clusterStampExecute(executeClusterStampStateMessageData);
    }
}
