package io.coti.zerospend.services;

import io.coti.basenode.data.messages.*;
import io.coti.basenode.services.BaseNodeGeneralVoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GeneralVoteService extends BaseNodeGeneralVoteService {

    @Override
    protected void continueHandleGeneralVoteMessage(boolean consensusReached, GeneralVoteMessage generalVoteMessage) {
        if (!consensusReached) {
            return;
        }
        switch (generalVoteMessage.getMessagePayload().getGeneralMessageType()) {
            case CLUSTER_STAMP_INDEX_VOTE:
                //todo start clusterstamp
                break;
            case CLUSTER_STAMP_HASH_VOTE:
                //todo start DB cleaning
                break;
            default:
                log.error("Unexpected vote type: {}", generalVoteMessage.getMessagePayload().getGeneralMessageType());
        }


    }
}
