package io.coti.basenode.data.messages;

import lombok.Data;

import java.time.Instant;

@Data
public abstract class ClusterStampStateMessageData extends StateMessageData {

    protected ClusterStampStateMessageData() {
    }

    protected ClusterStampStateMessageData(Instant createTime) {
        super(createTime);
    }
}
