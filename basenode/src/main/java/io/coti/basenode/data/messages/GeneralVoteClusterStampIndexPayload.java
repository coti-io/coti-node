package io.coti.basenode.data.messages;

import lombok.Data;

import java.nio.ByteBuffer;

@Data
public class GeneralVoteClusterStampIndexPayload extends MessagePayload {

//    public GeneralVoteClusterStampIndexPayload() {
//    }

    public GeneralVoteClusterStampIndexPayload() {
        super(GeneralMessageType.CLUSTER_STAMP_INDEX_VOTE);
    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] broadcastTypeBytes = generalMessageType.name().getBytes();
        return ByteBuffer.allocate(broadcastTypeBytes.length).put(broadcastTypeBytes).array();
    }

}
