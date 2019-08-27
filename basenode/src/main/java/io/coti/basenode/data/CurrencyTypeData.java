package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import java.time.Instant;

@Data
public class CurrencyTypeData implements IPropagatable, ISignable, ISignValidatable {

    protected CurrencyType currencyType;
    protected Instant creationTime;
    private Hash registrarHash;
    protected SignatureData registrarSignature;

    protected CurrencyTypeData() {
    }

    public CurrencyTypeData(CurrencyTypeData currencyTypeData) {
        currencyType = currencyTypeData.getCurrencyType();
        creationTime = currencyTypeData.getCreationTime();
        registrarSignature = currencyTypeData.getRegistrarSignature();
    }

    public CurrencyTypeData(CurrencyType currencyType, Instant creationTime, SignatureData registrarSignature) {
        this.currencyType = currencyType;
        this.creationTime = creationTime;
        this.registrarSignature = registrarSignature;
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

    @Override
    public Hash getHash() {
        return this.registrarHash;
    }

    @Override
    public void setHash(Hash hash) {

    }
}
