package io.coti.dspnode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.messages.StateMessage;
import io.coti.basenode.data.messages.StateMessageClusterStampExecutePayload;
import io.coti.basenode.data.messages.StateMessageClusterStampInitiatedPayload;
import io.coti.basenode.data.messages.StateMessageLastClusterStampIndexPayload;
import io.coti.basenode.services.BaseNodeStateMessageService;
import io.coti.basenode.services.interfaces.IClusterStampService;
import io.coti.basenode.services.interfaces.IGeneralVoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class StateMessageService extends BaseNodeStateMessageService {

    @Autowired
    private IClusterStampService clusterStampService;
    @Autowired
    private IGeneralVoteService generalVoteService;


    @Override
    public void continueHandleStateMessage(StateMessage stateMessage) {
        switch (stateMessage.getMessagePayload().getGeneralMessageType()) {
            case CLUSTER_STAMP_INITIATED:
                clusterStampService.clusterStampInitiate(stateMessage, (StateMessageClusterStampInitiatedPayload) stateMessage.getMessagePayload());
                break;
            case CLUSTER_STAMP_PREPARE_INDEX:
                generalVoteService.startCollectingVotes(stateMessage);
                generalVoteService.castVoteForClusterstampIndex(stateMessage.getHash(), clusterStampService.checkLastConfirmedIndex((StateMessageLastClusterStampIndexPayload) stateMessage.getMessagePayload()));
                break;
            case CLUSTER_STAMP_EXECUTE:
                clusterStampService.clusterStampExecute(stateMessage, (StateMessageClusterStampExecutePayload) stateMessage.getMessagePayload());
                break;
            default:
                log.error("Unexpected message type: {}", stateMessage.getMessagePayload().getGeneralMessageType());
        }
    }

}
