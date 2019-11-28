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
        this.name = tokenGenerationData.getName();
        this.symbol = tokenGenerationData.getSymbol();
        this.generatingCurrencyHash = tokenGenerationData.getGeneratingCurrencyHash().toString();
        this.totalSupply = tokenGenerationData.getTotalSupply();
        this.scale = tokenGenerationData.getScale();
        this.createTime = tokenGenerationData.getCreateTime();
        this.feeAmount = tokenGenerationData.getFeeAmount();
    }
}
