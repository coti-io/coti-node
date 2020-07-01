package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IEntity;
import io.coti.basenode.data.messages.GeneralVoteMessage;
import io.coti.basenode.data.messages.MessagePayload;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class GeneralVoteResult implements IEntity {

    private static final long serialVersionUID = -4221139981200256982L;
    private Hash hash;
    private Map<Hash, GeneralVoteMessage> hashToVoteMapping;
    private MessagePayload theMatter;
    private boolean isConsensusReached;
    private boolean isConsensusPositive;

    private GeneralVoteResult() {
    }

    public GeneralVoteResult(Hash hash, MessagePayload theMatter) {
        this.hash = hash;
        this.theMatter = theMatter;
        hashToVoteMapping = new HashMap<>();
        this.isConsensusReached = false;
        this.isConsensusPositive = false;
    }

}