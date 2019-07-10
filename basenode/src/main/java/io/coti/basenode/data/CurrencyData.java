package io.coti.basenode.data;

import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import java.time.Instant;

@Data
public class CurrencyData implements IPropagatable, ISignable, ISignValidatable {

    private long id;
    private String name;
    private String symbol;
    private String description;
    private long totalSupply;
    private Instant creationTime;
    private Hash registrarHash;
    private SignatureData registrarSignature;

    @Override
    public Hash getHash() {
        return new Hash(id);
    }

    @Override
    public void setHash(Hash hash) {

    }

    @Override
    public SignatureData getSignature() {
        return registrarSignature;
    }

    @Override
    public Hash getSignerHash() {
        return registrarHash;
    }

    @Override
    public void setSignerHash(Hash signerHash) {
        this.registrarHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        this.registrarSignature = signature;
    }
}
