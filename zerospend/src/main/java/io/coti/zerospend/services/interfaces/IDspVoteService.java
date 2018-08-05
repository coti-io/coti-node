package io.coti.zerospend.services.interfaces;

import io.coti.common.communication.DspVote;

public interface IDspVoteService {
    void insertVoteHelper(DspVote dspVote);
}
