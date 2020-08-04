package io.coti.financialserver.http.data;

import io.coti.basenode.data.TokenGenerationData;
import io.coti.basenode.http.data.interfaces.IResponseData;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TokenGenerationResponseData implements IResponseData {

    private OriginatorCurrencyResponseData originatorCurrencyData;
    private CurrencyTypeResponseData currencyTypeData;
    private BigDecimal feeAmount;

    public TokenGenerationResponseData(TokenGenerationData tokenGenerationData) {
        this.originatorCurrencyData = new OriginatorCurrencyResponseData(tokenGenerationData);
        this.currencyTypeData = new CurrencyTypeResponseData(tokenGenerationData);
        this.feeAmount = tokenGenerationData.getFeeAmount();
    }
}
