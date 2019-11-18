package io.coti.financialserver.http.data;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class GetMintingTokenQuoteData {

    private Map<String, GetMintingQuoteResponseData> mintingFeeQuotes;
    private BigDecimal totalRequestedSupply;
    private BigDecimal tokenMintedAmount;

    public GetMintingTokenQuoteData() {
    }

    public GetMintingTokenQuoteData(Map<String, GetMintingQuoteResponseData> mintingFeeQuotes, BigDecimal totalRequestedSupply, BigDecimal tokenMintedAmount) {
        this.mintingFeeQuotes = mintingFeeQuotes;
        this.totalRequestedSupply = totalRequestedSupply;
        this.tokenMintedAmount = tokenMintedAmount;
    }
}
