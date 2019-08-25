package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.data.interfaces.IPropagatable;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import java.time.Instant;

@Data
public class CurrencyTypeRegistrationData extends CurrencyTypeData implements IPropagatable, ISignable, ISignValidatable {

    private Hash currencyHash;
    private Hash registrarHash;

    private CurrencyTypeRegistrationData() {
    }

    public CurrencyTypeRegistrationData(CurrencyData currencyData) {
        super(currencyData.getCurrencyTypeData());
        this.currencyHash = currencyData.getHash();
        this.registrarHash = currencyData.getRegistrarHash();
    }

    public CurrencyTypeRegistrationData(Hash currencyHash, CurrencyType currencyType, Instant creationTime) {
        this.currencyHash = currencyHash;
        this.currencyType = currencyType;
        this.creationTime = creationTime;
    }

    @JsonIgnore
    @Override
    public Hash getHash() {
        return currencyHash;
    }

    @Override
    public void setHash(Hash hash) {
        currencyHash = hash;
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

    @Override
    public void setSignerHash(Hash signerHash) {
        registrarHash = signerHash;
    }

    @Override
    public void setSignature(SignatureData signature) {
        registrarSignature = signature;
    }
}
