package io.coti.financialserver.http.data;

import io.coti.basenode.data.TokenGenerationFeeDataInBaseTransaction;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TokenGenerationFeeDataResponseDetails implements Serializable {

    private String name;
    private String symbol;
    private String generatingCurrencyHash;
    private BigDecimal totalSupply;
    private int scale;
    private Instant requestTime;
    private BigDecimal feeQuote;

    public TokenGenerationFeeDataResponseDetails(TokenGenerationFeeDataInBaseTransaction tokenGenerationFeeDataInBaseTransaction) {
        this.name = tokenGenerationFeeDataInBaseTransaction.getName();
        this.symbol = tokenGenerationFeeDataInBaseTransaction.getSymbol();
        this.generatingCurrencyHash = tokenGenerationFeeDataInBaseTransaction.getGeneratingCurrencyHash().toString();
        this.totalSupply = tokenGenerationFeeDataInBaseTransaction.getTotalSupply();
        this.scale = tokenGenerationFeeDataInBaseTransaction.getScale();
        this.requestTime = tokenGenerationFeeDataInBaseTransaction.getRequestTime();
        this.feeQuote = tokenGenerationFeeDataInBaseTransaction.getFeeQuote();
    }
}
