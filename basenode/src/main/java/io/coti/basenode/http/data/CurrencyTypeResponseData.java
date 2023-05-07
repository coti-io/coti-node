package io.coti.basenode.http.data;

import io.coti.basenode.data.CurrencyRateSourceType;
import io.coti.basenode.data.CurrencyType;
import io.coti.basenode.data.CurrencyTypeData;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.http.data.interfaces.ITransactionResponseData;
import lombok.Data;

import java.time.Instant;

@Data
public class CurrencyTypeResponseData implements ITransactionResponseData {

    protected CurrencyType currencyType;
    protected Instant createTime;
    protected CurrencyRateSourceType currencyRateSourceType;
    protected String rateSource;
    protected String protectionModel;
    protected String signerHash;
    protected SignatureData signature;

    public CurrencyTypeResponseData() {
    }

    public CurrencyTypeResponseData(CurrencyTypeData currencyTypeData) {

        this.setCurrencyType(currencyTypeData.getCurrencyType());
        this.setCurrencyRateSourceType(currencyTypeData.getCurrencyRateSourceType());
        this.setCreateTime(currencyTypeData.getCreateTime());
        this.setSignature(currencyTypeData.getSignature());
        this.setProtectionModel(currencyTypeData.getProtectionModel());
        this.setSignerHash(currencyTypeData.getSignerHash().toString());
        this.setRateSource(currencyTypeData.getRateSource());

    }
}
