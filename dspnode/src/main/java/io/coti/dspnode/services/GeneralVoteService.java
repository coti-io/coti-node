package io.coti.dspnode.services;

import io.coti.basenode.data.messages.GeneralMessageType;
import io.coti.basenode.data.messages.GeneralVoteMessage;
import io.coti.basenode.services.BaseNodeGeneralVoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GeneralVoteService extends BaseNodeGeneralVoteService {

    @Override
    protected void continueHandleGeneralVoteMessage(boolean consensusReached, boolean consensusPositive, GeneralVoteMessage generalVoteMessage) {
        if (GeneralMessageType.CLUSTER_STAMP_HASH_HISTORY_NODE.equals(generalVoteMessage.getMessagePayload().getGeneralMessageType())
                && consensusReached && consensusPositive) {
            clusterStampService.setAgreedHistoryNodesNumberEnough();
        }
    }
}
