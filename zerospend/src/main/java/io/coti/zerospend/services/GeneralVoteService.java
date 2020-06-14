package io.coti.zerospend.services;

import io.coti.basenode.data.messages.GeneralVoteMessage;
import io.coti.basenode.services.BaseNodeGeneralVoteService;
import io.coti.basenode.services.interfaces.IClusterStampService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GeneralVoteService extends BaseNodeGeneralVoteService {

    @Autowired
    private IClusterStampService clusterStampService;

    @Override
    protected void continueHandleGeneralVoteMessage(boolean consensusReached, GeneralVoteMessage generalVoteMessage) {
        if (!consensusReached) {
            return;
        }
        switch (generalVoteMessage.getMessagePayload().getGeneralMessageType()) {
            case CLUSTER_STAMP_INDEX_VOTE:
                clusterStampService.calculateClusterStampDataAndHashes();
                break;
            case CLUSTER_STAMP_HASH_VOTE:
                clusterStampService.doClusterStampAfterVoting(generalVoteMessage);
                break;
            default:
                log.error("Unexpected vote type: {}", generalVoteMessage.getMessagePayload().getGeneralMessageType());
        }


    }
}
