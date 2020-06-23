package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotEmpty;
import javax.xml.bind.DatatypeConverter;
import java.io.Serializable;
import java.util.Arrays;

@Slf4j
@Data
public class Hash implements Serializable, Comparable<Hash> {

    private static final long serialVersionUID = 473304212781844813L;
    @NotEmpty
    private byte[] bytes;

    private Hash() {
    }

    public Hash(Long hashIndex) {
        this.bytes = Longs.toByteArray(hashIndex);
    }

    public Hash(Integer hashIndex) {
        this.bytes = Ints.toByteArray(hashIndex);
    }

    public Hash(String hash) {
        try {
            this.bytes = DatatypeConverter.parseHexBinary(hash);
        } catch (Exception e) {
            log.error("Illegal hash string: {}", hash);
            this.bytes = new byte[0];
        }
    }

    public Hash(byte[] bytes) {
        this.bytes = bytes;
    }

    public String toHexString() {
        return DatatypeConverter.printHexBinary(bytes).toLowerCase();
    }

    @JsonIgnore
    public boolean isNull() {
        return bytes == null;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Hash)) {
            return false;
        }
        return Arrays.equals(bytes, ((Hash) other).bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public String toString() {
        return toHexString();
    }

    @Override
    public int compareTo(Hash other) {
        return toHexString().compareTo(other.toHexString());
    }
}
