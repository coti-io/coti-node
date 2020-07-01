package io.coti.basenode.data.messages;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.nio.ByteBuffer;

@Data
public class GeneralVoteClusterStampHashPayload extends MessagePayload {

    private Hash clusterStampHash;

    public GeneralVoteClusterStampHashPayload() {}

    public GeneralVoteClusterStampHashPayload(Hash clusterStampHash) {
        super(GeneralMessageType.CLUSTER_STAMP_HASH_VOTE);
        this.clusterStampHash = clusterStampHash;
    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] broadcastTypeBytes = generalMessageType.name().getBytes();
        byte[] clusterStampHashBytes = clusterStampHash.getBytes();
        return ByteBuffer.allocate(broadcastTypeBytes.length + clusterStampHashBytes.length).put(broadcastTypeBytes).put(clusterStampHashBytes).array();
    }

}
