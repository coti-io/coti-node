package io.coti.basenode.data.messages;

import lombok.Data;

import java.time.Instant;

@Data
public abstract class StateMessageData extends MessageData {

    protected StateMessageData() {
    }

    protected StateMessageData(Instant createTime) {
        super(createTime);
    }

}
