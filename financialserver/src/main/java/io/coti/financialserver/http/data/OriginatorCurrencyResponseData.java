package io.coti.financialserver.http.data;

import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TokenGenerationServiceData;
import io.coti.basenode.http.data.interfaces.IResponseData;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OriginatorCurrencyResponseData implements IResponseData {

    private String name;
    private String symbol;
    private String description;
    private BigDecimal totalSupply;
    private int scale;
    private String originatorHash;
    private SignatureData originatorSignature;

    public OriginatorCurrencyResponseData(TokenGenerationServiceData tokenGenerationServiceData) {
        this.name = tokenGenerationServiceData.getOriginatorCurrencyData().getName();
        this.symbol = tokenGenerationServiceData.getOriginatorCurrencyData().getSymbol();
        this.description = tokenGenerationServiceData.getOriginatorCurrencyData().getDescription();
        this.totalSupply = tokenGenerationServiceData.getOriginatorCurrencyData().getTotalSupply();
        this.scale = tokenGenerationServiceData.getOriginatorCurrencyData().getScale();
        this.originatorHash = tokenGenerationServiceData.getOriginatorCurrencyData().getSignerHash().toString();
        this.originatorSignature = tokenGenerationServiceData.getOriginatorCurrencyData().getOriginatorSignature();
    }
}
