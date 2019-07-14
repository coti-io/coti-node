package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import java.nio.ByteBuffer;
import java.time.Instant;

@Data
public class CurrencyData implements IPropagatable, ISignable, ISignValidatable {

    private Hash hash;
    private String name;
    private String symbol;
    private String description;
    private long totalSupply;
    private Instant creationTime;
    private Hash registrarHash;
    private SignatureData registrarSignature;

    @Override
    public Hash getHash() {
        return hash;
    }

    @Override
    public void setHash(Hash hash) {
        this.hash = hash;
    }

    public void setHash() {
        byte[] nameInBytes = name.getBytes();
        byte[] symbolInBytes = symbol.getBytes();
        byte[] concatDataFields = ByteBuffer.allocate(nameInBytes.length + symbolInBytes.length).
                put(nameInBytes).put(symbolInBytes).array();

        hash = CryptoHelper.cryptoHash(concatDataFields, 224);
    }

    @JsonIgnore
    @Override
    public SignatureData getSignature() {
        return registrarSignature;
    }

    @JsonIgnore
    @Override
    public Hash getSignerHash() {
        return registrarHash;
    }

    @JsonIgnore
    @Override
    public void setSignerHash(Hash signerHash) {
        this.registrarHash = signerHash;
    }

    @JsonIgnore
    @Override
    public void setSignature(SignatureData signature) {
        this.registrarSignature = signature;
    }
}
