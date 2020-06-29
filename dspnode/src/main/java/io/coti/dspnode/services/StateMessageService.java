package io.coti.dspnode.services;

import io.coti.basenode.data.messages.*;
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
            case CLUSTER_STAMP_INITIATED:
                clusterStampService.clusterStampInitiate(stateMessage, (StateMessageClusterStampInitiatedPayload) stateMessage.getMessagePayload());
                break;
            case CLUSTER_STAMP_PREPARE_INDEX:
                clusterStampService.clusterStampContinueWithIndex(stateMessage);
                generalVoteService.startCollectingVotes(stateMessage);
                boolean vote = clusterStampService.checkLastConfirmedIndex((StateMessageLastClusterStampIndexPayload) stateMessage.getMessagePayload());
                generalVoteService.castVoteForClusterStampIndex(stateMessage.getHash(), vote);
                if (vote) {
                    clusterStampService.calculateClusterStampDataAndHashes(stateMessage.getCreateTime());  // todo separate it to a thread
                }
                break;
            case CLUSTER_STAMP_PREPARE_HASH:
                clusterStampService.clusterStampContinueWithHash(stateMessage);
                generalVoteService.startCollectingVotes(stateMessage);
                generalVoteService.castVoteForClusterStampHash(stateMessage.getHash(), clusterStampService.checkClusterStampHash((StateMessageClusterStampHashPayload) stateMessage.getMessagePayload()));
                break;
            case CLUSTER_STAMP_EXECUTE:
                clusterStampService.clusterStampExecute(stateMessage, (StateMessageClusterStampExecutePayload) stateMessage.getMessagePayload());
                break;
            default:
                log.error("Unexpected message type: {}", stateMessage.getMessagePayload().getGeneralMessageType());
        }
    }

}
