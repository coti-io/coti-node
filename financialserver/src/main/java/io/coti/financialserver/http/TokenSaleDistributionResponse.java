package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.http.data.TokenSaleDistributionResponseData;
import lombok.Data;

@Data
public class TokenSaleDistributionResponse extends BaseResponse {

    private TokenSaleDistributionResponseData tokenSaleDistributionResponseData;

    public TokenSaleDistributionResponse(TokenSaleDistributionResponseData tokenSaleDistributionResponseData) {
        super();
        this.tokenSaleDistributionResponseData = tokenSaleDistributionResponseData;
    }

}
