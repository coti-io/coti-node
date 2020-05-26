package io.coti.basenode.data.messages;

import lombok.Data;

@Data
public class GeneralVoteMessage extends GeneralMessage {

    private boolean vote;

    public GeneralVoteMessage(MessagePayload messagePayload, boolean vote) {
        super(messagePayload);
        this.vote = vote;
    }

    private GeneralVoteMessage() {
    }
}