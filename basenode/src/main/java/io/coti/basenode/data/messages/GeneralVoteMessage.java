package io.coti.basenode.data.messages;

import lombok.Data;

@Data
public class GeneralVoteMessage extends GeneralMessage {

    public GeneralVoteMessage(MessagePayload messagePayload) {
        super(messagePayload);
    }

    private GeneralVoteMessage() {
    }
}