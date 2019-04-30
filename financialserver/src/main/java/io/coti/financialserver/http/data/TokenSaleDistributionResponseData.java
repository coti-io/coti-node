package io.coti.financialserver.http.data;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TokenSaleDistributionResponseData {

    private Map<String, String> tokenSaleDistributionResults;

    public TokenSaleDistributionResponseData(Map<String, String> tokenSaleDistributionResults) {
        this.tokenSaleDistributionResults = tokenSaleDistributionResults;
    }
}
