package io.coti.financialserver.http.data;

import io.coti.basenode.data.CurrencyRateSourceType;
import io.coti.basenode.data.CurrencyType;
import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TokenGenerationData;
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

    public CurrencyTypeResponseData(TokenGenerationData tokenGenerationData) {
        this.currencyType = tokenGenerationData.getCurrencyTypeData().getCurrencyType();
        this.createTime = tokenGenerationData.getCurrencyTypeData().getCreateTime();
        this.currencyRateSourceType = tokenGenerationData.getCurrencyTypeData().getCurrencyRateSourceType();
        this.rateSource = tokenGenerationData.getCurrencyTypeData().getRateSource();
        this.protectionModel = tokenGenerationData.getCurrencyTypeData().getProtectionModel();
        this.signerHash = tokenGenerationData.getCurrencyTypeData().getSignerHash().toString();
        this.signature = tokenGenerationData.getCurrencyTypeData().getSignature();
    }
}
