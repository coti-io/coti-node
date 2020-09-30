package io.coti.basenode.data;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

@Data
public class CurrencyTypeData implements Serializable {

    private static final long serialVersionUID = -1294909068013238552L;
    @NotNull
    protected CurrencyType currencyType;
    @NotNull
    protected Instant createTime;
    protected CurrencyRateSourceType currencyRateSourceType;
    protected String rateSource;
    protected String protectionModel;
    @NotNull
    protected @Valid Hash signerHash;
    @NotNull
    protected @Valid SignatureData signature;

    protected CurrencyTypeData() {
    }

    public CurrencyTypeData(CurrencyType currencyType, Instant createTime) {
        this.currencyType = currencyType;
        this.createTime = createTime;
    }

    protected CurrencyTypeData(CurrencyTypeData currencyTypeData) {
        currencyType = currencyTypeData.getCurrencyType();
        createTime = currencyTypeData.getCreateTime();
        currencyRateSourceType = currencyTypeData.getCurrencyRateSourceType();
        rateSource = currencyTypeData.getRateSource();
        protectionModel = currencyTypeData.getProtectionModel();
        signerHash = currencyTypeData.getSignerHash();
        signature = currencyTypeData.getSignature();
    }
}
