package io.coti.financialserver.http.data;

import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TokenGenerationData;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

@Data
public class TokenCurrencyTypeResponseData implements Serializable {

    private String symbol;
    private String currencyType;
    private Instant createTime;
    private String currencyRateSourceType;
    private String rateSource;
    private String protectionModel;
    private String originatorHash;
    private SignatureData originatorSignature;

    public TokenCurrencyTypeResponseData(TokenGenerationData tokenGenerationData) {
        this.symbol = tokenGenerationData.getCurrencyTypeData().getSymbol();
        this.currencyType = tokenGenerationData.getCurrencyTypeData().getCurrencyType().name();
        this.createTime = tokenGenerationData.getCurrencyTypeData().getCreateTime();
        if (tokenGenerationData.getCurrencyTypeData().getCurrencyRateSourceType() != null) {
            this.currencyRateSourceType = tokenGenerationData.getCurrencyTypeData().getCurrencyRateSourceType().name();
        } else {
            this.currencyRateSourceType = null;
        }
        this.rateSource = tokenGenerationData.getCurrencyTypeData().getRateSource();
        this.protectionModel = tokenGenerationData.getCurrencyTypeData().getProtectionModel();
        this.originatorHash = tokenGenerationData.getCurrencyTypeData().getSignerHash().toString();
        this.originatorSignature = tokenGenerationData.getCurrencyTypeData().getOriginatorSignature();
    }
}
