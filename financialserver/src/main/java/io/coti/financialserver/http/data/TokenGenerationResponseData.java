package io.coti.financialserver.http.data;

import io.coti.basenode.data.TokenGenerationData;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TokenGenerationResponseData implements Serializable {

    private String name;
    private String symbol;
    private String generatingCurrencyHash;
    private BigDecimal totalSupply;
    private int scale;
    private Instant createTime;
    private BigDecimal feeAmount;

    public TokenGenerationResponseData(TokenGenerationData tokenGenerationData) {
        this.name = tokenGenerationData.getOriginatorCurrencyData().getName();
        this.symbol = tokenGenerationData.getOriginatorCurrencyData().getSymbol();
        this.generatingCurrencyHash = tokenGenerationData.getOriginatorCurrencyData().calculateHash().toString();
        this.totalSupply = tokenGenerationData.getOriginatorCurrencyData().getTotalSupply();
        this.scale = tokenGenerationData.getOriginatorCurrencyData().getScale();
        this.createTime = tokenGenerationData.getCurrencyTypeData().getCreateTime();
        this.feeAmount = tokenGenerationData.getFeeAmount();
    }
}
