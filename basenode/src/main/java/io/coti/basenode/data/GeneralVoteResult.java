package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.messages.MessagePayload;
import lombok.Data;

import java.util.Map;

@Data
public class GeneralVoteResult implements IEntity {

    private static final long serialVersionUID = -4221139981200256982L;
    private Hash hash;
    private Map<Hash, GeneralVote> hashToVoteMapping;
    private MessagePayload theMatter;
    private boolean isConsensus;
    private boolean whichConsensus;

    private GeneralVoteResult() {
    }

    public GeneralVoteResult(Hash hash, MessagePayload theMatter) {
        this.hash = hash;
        this.theMatter = theMatter;
    }

}