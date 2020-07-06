package io.coti.zerospend.services;

import io.coti.basenode.data.messages.VoteMessageData;
import io.coti.basenode.services.BaseNodeGeneralVoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static io.coti.basenode.data.messages.VoteMessageType.getName;

@Slf4j
@Service
public class GeneralVoteService extends BaseNodeGeneralVoteService {

    @Override
    protected void continueHandleGeneralVoteMessage(boolean consensusReached, boolean consensusPositive, VoteMessageData voteMessage) {
        if (!consensusReached) {
            return;
        }
        switch (Objects.requireNonNull(getName(voteMessage.getClass()))) {
            case CLUSTER_STAMP_AGREED_HASH_HISTORY_NODE:
                if (consensusPositive) {
                    clusterStampService.setAgreedHistoryNodesNumberEnough();
                    if (clusterStampHashVoteDone) {
                        clusterStampService.doClusterStampAfterVoting(voteMessage.getVoteHash());
                    }
                }
                break;
            case CLUSTER_STAMP_INDEX_VOTE:
                clusterStampService.calculateClusterStampDataAndHashesAndSendMessage(); // todo separate it to a thread
                break;
            case CLUSTER_STAMP_HASH_VOTE:
                clusterStampHashVoteDone = true;
                if (clusterStampService.isAgreedHistoryNodesNumberEnough()){
                    clusterStampService.doClusterStampAfterVoting(voteMessage.getVoteHash());
                }
                break;
            default:
                log.error("Unexpected vote type: {}", voteMessage.getClass());
        }
    }
}
