package io.coti.financialserver.http.data;

import io.coti.basenode.data.SignatureData;
import io.coti.basenode.data.TokenGenerationData;
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

    public OriginatorCurrencyResponseData(TokenGenerationData tokenGenerationData) {
        this.name = tokenGenerationData.getOriginatorCurrencyData().getName();
        this.symbol = tokenGenerationData.getOriginatorCurrencyData().getSymbol();
        this.description = tokenGenerationData.getOriginatorCurrencyData().getDescription();
        this.totalSupply = tokenGenerationData.getOriginatorCurrencyData().getTotalSupply();
        this.scale = tokenGenerationData.getOriginatorCurrencyData().getScale();
        this.originatorHash = tokenGenerationData.getOriginatorCurrencyData().getSignerHash().toString();
        this.originatorSignature = tokenGenerationData.getOriginatorCurrencyData().getOriginatorSignature();
    }
}
