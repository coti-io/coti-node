package io.coti.financialserver.http.data;

import io.coti.financialserver.http.TokenSaleDistributionResultData;
import lombok.Data;

import java.util.List;

@Data
public class TokenSaleDistributionResponseData {

    private List<TokenSaleDistributionResultData> tokenSaleDistributionResults;

    public TokenSaleDistributionResponseData(List<TokenSaleDistributionResultData> tokenSaleDistributionResults) {
        this.tokenSaleDistributionResults = tokenSaleDistributionResults;
    }
}
