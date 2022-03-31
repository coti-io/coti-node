package io.coti.financialserver.http.data;

import io.coti.basenode.data.CurrencyRateSourceType;
import io.coti.basenode.data.CurrencyType;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TokenGenerationServiceData;
import io.coti.basenode.http.data.interfaces.IResponseData;
import lombok.Data;

import java.time.Instant;

@Data
public class CurrencyTypeResponseData implements IResponseData {

    private CurrencyType currencyType;
    private Instant createTime;
    private CurrencyRateSourceType currencyRateSourceType;
    private String rateSource;
    private String protectionModel;
    private String signerHash;
    private SignatureData signature;

    public CurrencyTypeResponseData(TokenGenerationServiceData tokenGenerationServiceData) {
        this.currencyType = tokenGenerationServiceData.getCurrencyTypeData().getCurrencyType();
        this.createTime = tokenGenerationServiceData.getCurrencyTypeData().getCreateTime();
        this.currencyRateSourceType = tokenGenerationServiceData.getCurrencyTypeData().getCurrencyRateSourceType();
        this.rateSource = tokenGenerationServiceData.getCurrencyTypeData().getRateSource();
        this.protectionModel = tokenGenerationServiceData.getCurrencyTypeData().getProtectionModel();
        this.signerHash = tokenGenerationServiceData.getCurrencyTypeData().getSignerHash().toString();
        this.signature = tokenGenerationServiceData.getCurrencyTypeData().getSignature();
    }
}
