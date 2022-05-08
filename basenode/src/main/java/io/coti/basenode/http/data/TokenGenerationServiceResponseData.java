package io.coti.basenode.http.data;

import io.coti.basenode.data.BaseTransactionData;
import io.coti.basenode.data.TokenGenerationFeeBaseTransactionData;
import io.coti.basenode.http.data.interfaces.ITransactionResponseData;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TokenGenerationServiceResponseData implements ITransactionResponseData {

    private OriginatorCurrencyResponseData originatorCurrencyData;
    private CurrencyTypeResponseData currencyTypeData;
    private BigDecimal feeAmount;

    public TokenGenerationServiceResponseData(BaseTransactionData baseTransactionData) {
        originatorCurrencyData = new OriginatorCurrencyResponseData(((TokenGenerationFeeBaseTransactionData) baseTransactionData).getServiceData().getOriginatorCurrencyData());
        currencyTypeData = new CurrencyTypeResponseData(((TokenGenerationFeeBaseTransactionData) baseTransactionData).getServiceData().getCurrencyTypeData());
        this.feeAmount = ((TokenGenerationFeeBaseTransactionData) baseTransactionData).getServiceData().getFeeAmount();
    }
}
