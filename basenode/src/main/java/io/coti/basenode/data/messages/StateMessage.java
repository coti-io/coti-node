package io.coti.basenode.data.messages;

import lombok.Data;

import java.time.Instant;

@Data
public class StateMessage extends GeneralMessage {

    private StateMessage() {
    }

    public StateMessage(MessagePayload payload) {
        super(payload);
    }

    public StateMessage(MessagePayload payload, Instant createTime) {
        super(payload, createTime);
    }

}