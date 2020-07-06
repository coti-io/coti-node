package io.coti.basenode.data.messages;

import lombok.Data;

import java.time.Instant;

@Data
public abstract class StateMessageData extends MessageData {

    public StateMessageData() {
    }

    public StateMessageData(Instant createTime) {
        super(createTime);
    }

}