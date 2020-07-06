package io.coti.basenode.data.messages;

import io.coti.basenode.data.Hash;
import lombok.Data;

import java.nio.ByteBuffer;
import java.time.Instant;

@Data
public class HashClusterStampStateMessageData extends ClusterStampStateMessageData {

    private Hash clusterStampHash;

    public HashClusterStampStateMessageData() {
    }

    public HashClusterStampStateMessageData(Hash clusterStampHash, Instant createTime) {
        super(createTime);
        this.clusterStampHash = clusterStampHash;
    }

    @Override
    public byte[] getMessageInBytes() {
        byte[] typeBytes = StateMessageType.CLUSTER_STAMP_PREPARE_HASH.name().getBytes();
        byte[] clusterStampHashBytes = clusterStampHash.getBytes();
        return ByteBuffer.allocate(typeBytes.length + clusterStampHashBytes.length + Long.BYTES).put(typeBytes).put(clusterStampHashBytes).putLong(getCreateTime().toEpochMilli()).array();
    }

}