package io.coti.financialserver.http.data;

import io.coti.basenode.data.TokenGenerationServiceData;
import io.coti.basenode.http.data.interfaces.IResponseData;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TokenGenerationResponseData implements IResponseData {

    private OriginatorCurrencyResponseData originatorCurrencyData;
    private CurrencyTypeResponseData currencyTypeData;
    private BigDecimal feeAmount;

    public TokenGenerationResponseData(TokenGenerationServiceData tokenGenerationServiceData) {
        this.originatorCurrencyData = new OriginatorCurrencyResponseData(tokenGenerationServiceData);
        this.currencyTypeData = new CurrencyTypeResponseData(tokenGenerationServiceData);
        this.feeAmount = tokenGenerationServiceData.getFeeAmount();
    }
}
