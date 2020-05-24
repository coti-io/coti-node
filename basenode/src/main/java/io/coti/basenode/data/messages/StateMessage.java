package io.coti.basenode.data.messages;

import lombok.Data;

@Data
public class StateMessage extends GeneralMessage {

    private StateMessage() {
    }

    public StateMessage(MessagePayload payload) {
        super(payload);
    }


}