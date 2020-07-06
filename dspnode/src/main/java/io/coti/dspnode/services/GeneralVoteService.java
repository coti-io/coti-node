package io.coti.dspnode.services;

import io.coti.basenode.data.messages.VoteMessageData;
import io.coti.basenode.data.messages.VoteMessageType;
import io.coti.basenode.services.BaseNodeGeneralVoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static io.coti.basenode.data.messages.VoteMessageType.getName;

@Slf4j
@Service
public class GeneralVoteService extends BaseNodeGeneralVoteService {

    @Override
    protected void continueHandleGeneralVoteMessage(boolean consensusReached, boolean consensusPositive, VoteMessageData voteMessage) {
        if (VoteMessageType.CLUSTER_STAMP_AGREED_HASH_HISTORY_NODE.equals(getName(voteMessage.getClass()))
                && consensusReached && consensusPositive) {
            clusterStampService.setAgreedHistoryNodesNumberEnough();
        }
    }
}
