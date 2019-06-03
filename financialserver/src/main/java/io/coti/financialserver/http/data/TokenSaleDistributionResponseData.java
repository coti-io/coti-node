package io.coti.financialserver.http.data;

import lombok.Data;

import java.util.List;

@Data
public class TokenSaleDistributionResponseData {

    private List<TokenSaleDistributionResultData> tokenSaleDistributionResults;

    public TokenSaleDistributionResponseData(List<TokenSaleDistributionResultData> tokenSaleDistributionResults) {
        this.tokenSaleDistributionResults = tokenSaleDistributionResults;
    }
}
