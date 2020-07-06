package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.messages.MessageData;
import io.coti.basenode.data.messages.VoteMessageData;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class VoteResult implements IEntity {

    private static final long serialVersionUID = 7271952147459726372L;
    private Hash hash;
    private Map<Hash, VoteMessageData> hashToVoteMapping;
    private MessageData theMatterOfVoting;
    private boolean isConsensusReached;
    private boolean isConsensusPositive;

    private VoteResult() {
    }

    public VoteResult(Hash hash, MessageData theMatterOfVoting) {
        this.hash = hash;
        this.theMatterOfVoting = theMatterOfVoting;
        hashToVoteMapping = new HashMap<>();
        this.isConsensusReached = false;
        this.isConsensusPositive = false;
    }

}