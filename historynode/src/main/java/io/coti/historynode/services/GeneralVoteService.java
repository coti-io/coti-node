package io.coti.historynode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.messages.GeneralVoteClusterStampHashPayload;
import io.coti.basenode.data.messages.GeneralVoteClusterStampHistoryNodeAgreedHashPayload;
import io.coti.basenode.data.messages.GeneralVoteMessage;
import io.coti.basenode.services.BaseNodeGeneralVoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GeneralVoteService extends BaseNodeGeneralVoteService {

    @Override
    public GeneralVoteMessage castVoteForClusterStampHash(Hash voteHash, boolean vote, Hash clusterStampHash) {
        GeneralVoteClusterStampHistoryNodeAgreedHashPayload generalVoteClusterStampHistoryNodeAgreedHashPayload = new GeneralVoteClusterStampHistoryNodeAgreedHashPayload(clusterStampHash);
        return castVote(generalVoteClusterStampHistoryNodeAgreedHashPayload, voteHash, vote, "clusterstamp hash");
    }

}
