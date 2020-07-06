package io.coti.historynode.services;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.messages.AgreedHashClusterStampVoteMessageData;
import io.coti.basenode.data.messages.VoteMessageData;
import io.coti.basenode.services.BaseNodeGeneralVoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
public class GeneralVoteService extends BaseNodeGeneralVoteService {

    @Override
    public VoteMessageData castVoteForClusterStampHash(Hash voteHash, boolean vote, Hash clusterStampHash) {
        AgreedHashClusterStampVoteMessageData agreedHashClusterStampVoteMessageData = new AgreedHashClusterStampVoteMessageData(clusterStampHash, voteHash, vote, Instant.now());
        castVote(agreedHashClusterStampVoteMessageData, "clusterstamp hash");
        return agreedHashClusterStampVoteMessageData;
    }

}
