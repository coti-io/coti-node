package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    private ClusterStampNameData() {
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

    private void generateAndSetHash() {
        byte[] typeInBytes = Integer.toString(this.type.ordinal()).getBytes();
        byte[] versionTimeMillisInBytes = ByteBuffer.allocate(Long.BYTES).putLong(this.versionTimeMillis).array();
        byte[] creationTimeMillisInBytes = ByteBuffer.allocate(Long.BYTES).putLong(this.creationTimeMillis).array();
        byte[] concatDataFields = ByteBuffer.allocate(typeInBytes.length + versionTimeMillisInBytes.length + creationTimeMillisInBytes.length)
                .put(typeInBytes).put(versionTimeMillisInBytes).put(creationTimeMillisInBytes).array();
        this.hash = CryptoHelper.cryptoHash(concatDataFields);
    }

    @JsonIgnore
    public boolean isMajor() {
        return type == ClusterStampType.MAJOR;
    }

    @JsonIgnore
    public boolean isCurrencies() {
        return type == ClusterStampType.CURRENCIES;
    }
}
