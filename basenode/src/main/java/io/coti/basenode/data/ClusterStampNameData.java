package io.coti.basenode.data;

import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.interfaces.IEntity;
import lombok.Data;

import java.nio.ByteBuffer;
import java.time.Instant;

@Data
public class ClusterStampNameData implements IEntity {

    private Long versionTimeMillis;
    private Long creationTimeMillis;
    private Hash hash;

    public ClusterStampNameData(String versionTimeMillis, String creationTimeMillis) {
        this.versionTimeMillis = Long.parseLong(versionTimeMillis);
        this.creationTimeMillis = Long.parseLong(creationTimeMillis);
        generateAndSetHash();
    }

    public ClusterStampNameData() {
        this.versionTimeMillis = Instant.now().toEpochMilli();
        this.creationTimeMillis = versionTimeMillis;
        generateAndSetHash();
    }

    public ClusterStampNameData(Long versionTimeMillis) {
        this.versionTimeMillis = versionTimeMillis;
        this.creationTimeMillis = Instant.now().toEpochMilli();
        generateAndSetHash();
    }

    private void generateAndSetHash() {
        byte[] versionTimeMillisInBytes = ByteBuffer.allocate(Long.BYTES).putLong(this.versionTimeMillis).array();
        byte[] creationTimeMillisInBytes = ByteBuffer.allocate(Long.BYTES).putLong(this.creationTimeMillis).array();
        byte[] concatDataFields = ByteBuffer.allocate(versionTimeMillisInBytes.length + creationTimeMillisInBytes.length)
                .put(versionTimeMillisInBytes).put(creationTimeMillisInBytes).array();
        this.hash = CryptoHelper.cryptoHash(concatDataFields);
    }
}
