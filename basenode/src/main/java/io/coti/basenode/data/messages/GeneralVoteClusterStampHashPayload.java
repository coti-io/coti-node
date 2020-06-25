package io.coti.basenode.data.messages;

import lombok.Data;

import java.nio.ByteBuffer;

@Data
public class GeneralVoteClusterStampHashPayload extends MessagePayload {

    public GeneralVoteClusterStampHashPayload() {
        super(GeneralMessageType.CLUSTER_STAMP_HASH_VOTE);
    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] broadcastTypeBytes = generalMessageType.name().getBytes();
        return ByteBuffer.allocate(broadcastTypeBytes.length).put(broadcastTypeBytes).array();
    }

}
