package io.coti.historynode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.messages.AgreedHashClusterStampVoteMessageData;
import io.coti.basenode.data.messages.VoteMessageData;
import io.coti.basenode.services.BaseNodeVoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
public class VoteService extends BaseNodeVoteService {

    @Override
    public VoteMessageData castVoteForClusterStampHash(boolean vote, Hash clusterStampHash) {
        AgreedHashClusterStampVoteMessageData agreedHashClusterStampVoteMessageData = new AgreedHashClusterStampVoteMessageData(clusterStampHash, vote, Instant.now());
        castVote(agreedHashClusterStampVoteMessageData, "clusterstamp hash");
        return agreedHashClusterStampVoteMessageData;
    }

}
