package io.coti.historynode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.messages.*;
import io.coti.basenode.services.BaseNodeStateMessageService;
import io.coti.basenode.services.interfaces.IClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static io.coti.basenode.data.messages.StateMessageType.getName;

@Slf4j
@Service
public class StateMessageService extends BaseNodeStateMessageService {

    @Autowired
    private IClusterStampService clusterStampService;
    @Autowired
    private TransactionService transactionService;


    @Override
    public void continueHandleStateMessage(StateMessageData stateMessage) {
        switch (Objects.requireNonNull(getName(stateMessage.getClass()))) {
            case CLUSTER_STAMP_INITIATED:
                clusterStampService.clusterStampInitiate(stateMessage, (InitiateClusterStampStateMessageData) stateMessage);
                break;
            case CLUSTER_STAMP_PREPARE_INDEX:
                clusterStampService.clusterStampContinueWithIndex((LastIndexClusterStampStateMessageData) stateMessage);
                transactionService.setHistoryProcessingPause();
                boolean vote = clusterStampService.checkLastConfirmedIndex((LastIndexClusterStampStateMessageData) stateMessage);
                if (vote) {
                    clusterStampService.calculateClusterStampDataAndHashes();  // todo separate it to a thread
                }
                break;
            case CLUSTER_STAMP_PREPARE_HASH:
                Hash candidateClusterStampHash = clusterStampService.getCandidateClusterStampHash();
                voteService.castVoteForClusterStampHash(clusterStampService.checkClusterStampHash((HashClusterStampStateMessageData) stateMessage),
                        candidateClusterStampHash);
                break;
            case CLUSTER_STAMP_EXECUTE:
                clusterStampService.clusterStampExecute((ExecuteClusterStampStateMessageData) stateMessage);
                break;
            default:
        }
    }

}
