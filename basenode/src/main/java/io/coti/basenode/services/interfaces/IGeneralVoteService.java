package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.messages.VoteMessageData;
import io.coti.basenode.data.messages.StateMessageData;
import io.coti.basenode.data.messages.VoteMessageType;

import java.util.List;

public interface IGeneralVoteService {

    void init();

    void handleGeneralVoting(VoteMessageData generalVoteMessage);

    void startCollectingVotes(StateMessageData voteHash, VoteMessageData myVote);

    VoteMessageData castVoteForClusterStampIndex(Hash voteHash, boolean vote);

    VoteMessageData castVoteForClusterStampHash(Hash voteHash, boolean vote, Hash clusterStampHash);

    List<VoteMessageData> getVoteResultVotersList(Hash voteHash);

    long calculateQuorumOfValidators(VoteMessageType messageType);

    void clearClusterStampHashVoteDone();
}
