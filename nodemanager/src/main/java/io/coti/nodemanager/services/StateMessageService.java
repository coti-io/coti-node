package io.coti.nodemanager.services;

import io.coti.basenode.data.messages.StateMessage;
import io.coti.basenode.data.messages.StateMessageClusterStampExecutePayload;
import io.coti.basenode.services.BaseNodeStateMessageService;
import io.coti.basenode.services.interfaces.IClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StateMessageService extends BaseNodeStateMessageService {

    @Autowired
    private IClusterStampService clusterStampService;

    @Override
    public void continueHandleStateMessage(StateMessage stateMessage) {
        switch (stateMessage.getMessagePayload().getGeneralMessageType()) {
            case CLUSTER_STAMP_EXECUTE:
                clusterStampService.clusterStampExecute(stateMessage, (StateMessageClusterStampExecutePayload) stateMessage.getMessagePayload());
                break;
            case CLUSTER_STAMP_PREPARE_INDEX:
                generalVoteService.startCollectingVotes(stateMessage, null);
                break;
            case CLUSTER_STAMP_INITIATED:
                break;
            case CLUSTER_STAMP_CONTINUE:
                // todo
                break;
            default:
                log.error("Unexpected message type: {}", stateMessage.getMessagePayload().getGeneralMessageType());
        }
    }

}
