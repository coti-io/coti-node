package io.coti.nodemanager.services;

import io.coti.basenode.data.messages.StateMessage;
import io.coti.basenode.data.messages.StateMessageClusterStampExecutePayload;
import io.coti.basenode.services.BaseNodeClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ClusterStampService extends BaseNodeClusterStampService {

    @Override
    public void clusterStampExecute(StateMessage stateMessage, StateMessageClusterStampExecutePayload stateMessageClusterStampExecutePayload) {
// todo
    }
}
