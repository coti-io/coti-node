package io.coti.financialserver.http.data;

import io.coti.basenode.http.data.interfaces.IResponseData;
import lombok.Data;

import java.util.List;

@Data
public class TokenSaleDistributionResponseData implements IResponseData {

    private List<TokenSaleDistributionResultData> tokenSaleDistributionResults;

    public TokenSaleDistributionResponseData(List<TokenSaleDistributionResultData> tokenSaleDistributionResults) {
        this.tokenSaleDistributionResults = tokenSaleDistributionResults;
    }
}
