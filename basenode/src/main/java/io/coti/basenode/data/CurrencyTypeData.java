package io.coti.basenode.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.coti.basenode.crypto.CryptoHelper;
import io.coti.basenode.data.interfaces.ISignValidatable;
import io.coti.basenode.data.interfaces.ISignable;
import lombok.Data;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.time.Instant;

@Data
public class CurrencyTypeData implements ISignable, ISignValidatable, Serializable {
    protected CurrencyType currencyType;
    protected Instant creationTime;
    private Hash hash;
    protected SignatureData registrarSignature;

    public void setHash() {
        byte[] typeTextInBytes = currencyType.getText().getBytes();
        final long creationTimeAsLong = this.creationTime.toEpochMilli();
        byte[] concatDataFields = ByteBuffer.allocate(typeTextInBytes.length + Long.BYTES).
                put(typeTextInBytes).putLong(creationTimeAsLong).array();
        this.hash = CryptoHelper.cryptoHash(concatDataFields);
    }

    protected CurrencyTypeData() {
    }

    public CurrencyTypeData(CurrencyTypeData currencyTypeData) {
        currencyType = currencyTypeData.getCurrencyType();
        creationTime = currencyTypeData.getCreationTime();
        registrarSignature = currencyTypeData.getRegistrarSignature();
        hash = currencyTypeData.getHash();
    }

    public CurrencyTypeData(CurrencyType currencyType, Instant creationTime) {
        this.currencyType = currencyType;
        this.creationTime = creationTime;
        this.registrarSignature = null;
        setHash();
    }

    @JsonIgnore
    @Override
    public SignatureData getSignature() {
        return registrarSignature;
    }

    @Override
    public Hash getSignerHash() {
        return null;
    }

    @Override
    public void setSignerHash(Hash signerHash) {

    }

    @JsonIgnore
    @Override
    public void setSignature(SignatureData signature) {
        this.registrarSignature = signature;
    }

}
