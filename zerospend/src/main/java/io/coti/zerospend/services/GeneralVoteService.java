package io.coti.zerospend.services;

import io.coti.basenode.data.messages.GeneralVoteMessage;
import io.coti.basenode.services.BaseNodeGeneralVoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GeneralVoteService extends BaseNodeGeneralVoteService {

    @Override
    protected void continueHandleGeneralVoteMessage(boolean consensusReached, boolean consensusPositive, GeneralVoteMessage generalVoteMessage) {
        if (!consensusReached) {
            return;
        }
        switch (generalVoteMessage.getMessagePayload().getGeneralMessageType()) {
            case CLUSTER_STAMP_HASH_HISTORY_NODE:
                if (consensusPositive) {
                    clusterStampService.setAgreedHistoryNodesNumberEnough();
                    if (clusterStampHashVoteDone) {
                        clusterStampService.doClusterStampAfterVoting(generalVoteMessage.getVoteHash());
                    }
                }
                break;
            case CLUSTER_STAMP_INDEX_VOTE:
                clusterStampService.calculateClusterStampDataAndHashesAndSendMessage(); // todo separate it to a thread
                break;
            case CLUSTER_STAMP_HASH_VOTE:
                clusterStampHashVoteDone = true;
                if (clusterStampService.isAgreedHistoryNodesNumberEnough()){
                    clusterStampService.doClusterStampAfterVoting(generalVoteMessage.getVoteHash());
                }
                break;
            default:
                log.error("Unexpected vote type: {}", generalVoteMessage.getMessagePayload().getGeneralMessageType());
        }
    }
}
