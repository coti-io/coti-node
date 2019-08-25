package io.coti.basenode.data;

import lombok.Data;

import java.time.Instant;

@Data
public class CurrencyTypeData {

    protected CurrencyType currencyType;
    protected Instant creationTime;
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
}
