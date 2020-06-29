package io.coti.basenode.data.messages;

import lombok.Data;

import java.nio.ByteBuffer;

@Data
public class GeneralVoteClusterStampHistoryNodeAgreedHashPayload extends MessagePayload {

    public GeneralVoteClusterStampHistoryNodeAgreedHashPayload() {
        super(GeneralMessageType.CLUSTER_STAMP_HASH_HISTORY_NODE);
    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] broadcastTypeBytes = generalMessageType.name().getBytes();
        return ByteBuffer.allocate(broadcastTypeBytes.length).put(broadcastTypeBytes).array();
    }

}
