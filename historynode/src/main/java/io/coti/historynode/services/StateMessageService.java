package io.coti.historynode.services;

import io.coti.basenode.data.messages.StateMessage;
import io.coti.basenode.data.messages.StateMessageClusterStampExecutePayload;
import io.coti.basenode.data.messages.StateMessageClusterStampHashPayload;
import io.coti.basenode.data.messages.StateMessageLastClusterStampIndexPayload;
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
    @Autowired
    private TransactionService transactionService;


    @Override
    public void continueHandleStateMessage(StateMessage stateMessage) {
        switch (stateMessage.getMessagePayload().getGeneralMessageType()) {
            case CLUSTER_STAMP_PREPARE_INDEX:
                transactionService.setHistoryProcessingPause(); // todo check to restart it
                boolean vote = clusterStampService.checkLastConfirmedIndex((StateMessageLastClusterStampIndexPayload) stateMessage.getMessagePayload());
                if (vote) {
                    clusterStampService.calculateClusterStampDataAndHashes();  // todo separate it to a thread
                }
                break;
            case CLUSTER_STAMP_PREPARE_HASH:
                generalVoteService.castVoteForClusterStampHash(stateMessage.getHash(),
                        clusterStampService.checkClusterStampHash((StateMessageClusterStampHashPayload) stateMessage.getMessagePayload()));
                break;
            case CLUSTER_STAMP_CONTINUE:
                // todo
                break;
            case CLUSTER_STAMP_EXECUTE:
                clusterStampService.clusterStampExecute(stateMessage, (StateMessageClusterStampExecutePayload) stateMessage.getMessagePayload());
                break;
            default:
        }
    }

}
