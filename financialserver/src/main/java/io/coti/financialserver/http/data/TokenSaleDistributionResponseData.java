package io.coti.financialserver.http.data;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TokenSaleDistributionResponseData implements Serializable {

    private List<TokenSaleDistributionResultData> tokenSaleDistributionResults;

    public TokenSaleDistributionResponseData(List<TokenSaleDistributionResultData> tokenSaleDistributionResults) {
        this.tokenSaleDistributionResults = tokenSaleDistributionResults;
    }
}
