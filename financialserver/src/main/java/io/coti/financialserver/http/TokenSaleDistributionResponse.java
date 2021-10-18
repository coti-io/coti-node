package io.coti.financialserver.http;

import io.coti.basenode.http.BaseResponse;
import io.coti.financialserver.http.data.TokenSaleDistributionResponseData;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TokenSaleDistributionResponse extends BaseResponse {

    private TokenSaleDistributionResponseData tokenSaleDistributionResponseData;

    public TokenSaleDistributionResponse(TokenSaleDistributionResponseData tokenSaleDistributionResponseData) {
        this.tokenSaleDistributionResponseData = tokenSaleDistributionResponseData;
    }

}
