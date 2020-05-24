package io.coti.basenode.services.interfaces;

import io.coti.basenode.data.messages.GeneralVoteMessage;
import io.coti.basenode.data.Hash;

public interface IGeneralVoteService {

    void init();

    void handleGeneralVoting(GeneralVoteMessage generalVoteMessage);

    void castVoteForClusterstampIndex(Hash voteHash, boolean vote);
}
