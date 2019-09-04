package io.coti.basenode.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.nio.ByteBuffer;
import java.time.Instant;

@Data
public class ClusterStampNameData implements IEntity {

    private ClusterStampType type;
    private Long versionTimeMillis;
    private Long creationTimeMillis;
    private Hash hash;

    public ClusterStampNameData() {

    }

    public ClusterStampNameData(ClusterStampType type, String versionTimeMillis, String creationTimeMillis) {
        this.type = type;
        this.versionTimeMillis = Long.parseLong(versionTimeMillis);
        this.creationTimeMillis = Long.parseLong(creationTimeMillis);
        generateAndSetHash();
    }

    public ClusterStampNameData(ClusterStampType type) {
        this.type = type;
        this.versionTimeMillis = Instant.now().toEpochMilli();
        this.creationTimeMillis = versionTimeMillis;
        generateAndSetHash();
    }

    public ClusterStampNameData(ClusterStampType type, Long versionTimeMillis) {
        this.type = type;
        this.versionTimeMillis = versionTimeMillis;
        this.creationTimeMillis = Instant.now().toEpochMilli();
        generateAndSetHash();
    }

    public String getClusterStampFileName() {
        StringBuilder sb = new StringBuilder("Clusterstamp_");
        sb.append(type.getMark()).append("_").append(versionTimeMillis.toString());
        if (!versionTimeMillis.equals(creationTimeMillis)) {
            sb.append("_").append(creationTimeMillis.toString());
        }
        return sb.append(".csv").toString();
    }


    private void generateAndSetHash() {
        byte[] typeInBytes = Integer.toString(this.type.ordinal()).getBytes();
        byte[] versionTimeInBytes = this.versionTimeMillis.toString().getBytes();
        byte[] creationTimeInBytes = this.creationTimeMillis.toString().getBytes();
        byte[] concatDataFields = ByteBuffer.allocate(typeInBytes.length + versionTimeInBytes.length + creationTimeInBytes.length)
                .put(typeInBytes).put(versionTimeInBytes).put(creationTimeInBytes).array();
        this.hash = CryptoHelper.cryptoHash(concatDataFields);
    }

    public boolean isMajor() {
        return type == ClusterStampType.MAJOR;
    }
}
