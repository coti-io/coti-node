package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.messages.GeneralVoteMessage;
import io.coti.basenode.data.Hash;
import io.coti.basenode.data.messages.StateMessage;

import java.util.List;

public interface IGeneralVoteService {

    void init();

    void handleGeneralVoting(GeneralVoteMessage generalVoteMessage);

    void startCollectingVotes(StateMessage voteHash, GeneralVoteMessage myVote);

    GeneralVoteMessage castVoteForClusterStampIndex(Hash voteHash, boolean vote);

    GeneralVoteMessage castVoteForClusterStampHash(Hash voteHash, boolean vote);

    List<GeneralVoteMessage> getVoteResultVotersList(Hash voteHash);

    long calculateQuorumOfValidators(long numberOfValidators);

}
