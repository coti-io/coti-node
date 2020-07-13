package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.Hash;
import io.coti.basenode.data.messages.VoteMessageData;
import io.coti.basenode.data.messages.StateMessageData;
import io.coti.basenode.data.messages.VoteMessageType;

import java.util.List;

public interface IVoteService {

    void init();

    void handleVoteMessage(VoteMessageData generalVoteMessage);

    void startCollectingVotes(StateMessageData stateMessage, Hash voteHash, VoteMessageData myVote);

    VoteMessageData castVoteForClusterStampIndex(Hash voteHash, boolean vote);

    VoteMessageData castVoteForClusterStampHash(boolean vote, Hash clusterStampHash);

    List<VoteMessageData> getVoteResultVotersList(VoteMessageType clusterStampHashVote, Hash voteHash);

    long calculateQuorumOfValidators(VoteMessageType messageType);

    void clearClusterStampHashVoteDone();
}
